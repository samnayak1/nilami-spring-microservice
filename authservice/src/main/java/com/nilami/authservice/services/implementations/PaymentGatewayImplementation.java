package com.nilami.authservice.services.implementations;


import org.springframework.stereotype.Service;

import com.nilami.authservice.dto.CreatePaymentGatewayResponse;
import com.nilami.authservice.dto.UserDTO;

import com.nilami.authservice.services.PaymentGatewayService;
import com.nilami.authservice.services.UserService;
import com.stripe.param.PaymentIntentCreateParams;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentGatewayImplementation implements PaymentGatewayService{


    private final UserService userService;

    @Override
    public CreatePaymentGatewayResponse createPaymentIntent(String userId, long amount, String currency) {

        UserDTO user = userService.getUserDetails(userId);

        if(user == null){
            throw new RuntimeException("User not found with id: "+userId);
        }

      PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
        .setAmount(amount * 100L) // Amount in cents. Send usd from the frontend because that is kinda the "lingua franca" of currencies
        .setCurrency(currency)
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
    
    
}
