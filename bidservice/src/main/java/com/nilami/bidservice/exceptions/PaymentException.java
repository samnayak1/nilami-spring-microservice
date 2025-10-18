package com.nilami.bidservice.exceptions;

import java.math.BigDecimal;

public class PaymentException extends RuntimeException {
public PaymentException(String message,String userId,BigDecimal placedBidAmount) {
        super(message);
        //TODO: change the argumens
    }
}