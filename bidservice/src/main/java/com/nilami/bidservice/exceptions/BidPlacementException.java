package com.nilami.bidservice.exceptions;

public class BidPlacementException extends RuntimeException {
    public BidPlacementException(String message) {
        super(message);
    }
    
    public BidPlacementException(String message, Throwable cause) {
        super(message, cause);
    }
}