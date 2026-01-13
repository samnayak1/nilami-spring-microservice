package com.nilami.bidservice.exceptions;

public class IdempotentKeyException extends RuntimeException{
    public IdempotentKeyException(String message) {
        super(message);
    }
}
