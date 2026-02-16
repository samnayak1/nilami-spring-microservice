package com.nilami.authservice.controllers.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nilami.authservice.controllers.requestTypes.CreatePaymentIntentRequest;
import com.nilami.authservice.dto.CreatePaymentGatewayResponse;
import com.nilami.authservice.services.PaymentGatewayService;

import com.stripe.exception.StripeException;


import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth/payment")
@RequiredArgsConstructor
public class PaymentGatewayController {

    
    private final PaymentGatewayService paymentGatewayService;


    @PostMapping("/create-payment-intent")
    public ResponseEntity<CreatePaymentGatewayResponse> createPaymentIntent(
            @RequestBody CreatePaymentIntentRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId)
                throws StripeException {

         CreatePaymentGatewayResponse response = paymentGatewayService
            .createPaymentIntent(userId, request.getAmount(), request.getCurrency());

        return ResponseEntity.ok(response);
    }

        @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
     Boolean success = paymentGatewayService.handleWebhook(payload, sigHeader);

        return ResponseEntity.ok("Success"+success);
    }


}
