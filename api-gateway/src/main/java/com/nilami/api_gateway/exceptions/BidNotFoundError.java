package com.nilami.api_gateway.exceptions;


public class BidNotFoundError extends RuntimeException {
    public BidNotFoundError(String message) {
        super(message);
    }
     public BidNotFoundError(String message,Throwable ex) {
        super(message,ex);
    }
    }