package com.nilami.bidservice.exceptions;

public class ItemExpiredException extends RuntimeException {
    public ItemExpiredException(String message) {
        super(message);
    }
}