package com.nilami.bidservice.exceptions;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {

    private final String userId;
    private final BigDecimal placedBidAmount;

    public PaymentException(String message, String userId, BigDecimal placedBidAmount) {
        super(message);
        this.userId = userId;
        this.placedBidAmount = placedBidAmount;
    }
}