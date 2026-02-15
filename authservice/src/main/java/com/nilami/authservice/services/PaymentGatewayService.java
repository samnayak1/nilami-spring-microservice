package com.nilami.authservice.services;

import com.nilami.authservice.dto.CreatePaymentGatewayResponse;

public interface PaymentGatewayService {
    CreatePaymentGatewayResponse createPaymentIntent(String userId, long amount, String currency);
    

}
