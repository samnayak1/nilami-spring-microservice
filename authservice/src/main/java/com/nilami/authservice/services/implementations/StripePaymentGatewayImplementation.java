package com.nilami.authservice.services.implementations;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import com.nilami.authservice.dto.CreatePaymentGatewayResponse;
import com.nilami.authservice.dto.UserDTO;
import com.nilami.authservice.services.PaymentGatewayService;
import com.nilami.authservice.services.UserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentGatewayImplementation implements PaymentGatewayService {

    private final UserService userService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Override
    public CreatePaymentGatewayResponse createPaymentIntent(String userId, long amount, String currency) {

        UserDTO user = userService.getUserDetails(userId);

        if (user == null) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount * 100L) // Amount in cents. Send usd from the frontend because that is kinda the
                                          // "lingua franca" of currencies
                .setCurrency(currency)
                .putMetadata("userId", userId)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        CreatePaymentGatewayResponse response = new CreatePaymentGatewayResponse();
        try {
            com.stripe.model.PaymentIntent paymentIntent = com.stripe.model.PaymentIntent.create(params);
            response.setClientSecret(paymentIntent.getClientSecret());
            response.setPaymentIntentId(paymentIntent.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage(), e);
        }

        return response;

    }

    @Override
    public Boolean handleWebhook(String payload, String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error(" Webhook signature verification failed", e);
            return false;
        }

        log.info(" Received event: {}", event.getType());

        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;

            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;

            case "charge.succeeded":
            case "charge.updated":
            case "payment_intent.created":
                log.info("Ignoring event: {}", event.getType());
                break;

            default:
                log.warn("Unhandled event type: {}", event.getType());
        }

        return true;
    }

    private void handlePaymentIntentSucceeded(Event event) {
        try {
            System.out.println("Processing payment_intent.succeeded event: " + event.getId());
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject;

            if (dataObjectDeserializer.getObject().isPresent()) {
                stripeObject = dataObjectDeserializer.getObject().get();
            } else {

                stripeObject = dataObjectDeserializer.deserializeUnsafe();
            }

            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;

            String userId = paymentIntent.getMetadata().get("userId");
            Long amountInCents = paymentIntent.getAmount();

            if (userId == null || userId.isEmpty()) {
                log.warn("Payment succeeded but userId is missing in metadata");
                return;
            }

            BigDecimal amount = BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100));

            // Update user's balance in database
            userService.addBankBalanceToUser(userId, amount);

            log.info(" Payment succeeded for user: {} - Amount: ${}", userId, amount);

        } catch (Exception e) {
            log.error(" Error processing payment_intent.succeeded", e);
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize event"));

            String userId = paymentIntent.getMetadata().get("userId");
            String failureReason = paymentIntent.getLastPaymentError() != null
                    ? paymentIntent.getLastPaymentError().getMessage()
                    : "Unknown error";

            log.error(" Payment failed for user: {} - Reason: {}", userId, failureReason);

        } catch (Exception e) {
            log.error(" Error processing payment_intent.payment_failed", e);
        }
    }

}
