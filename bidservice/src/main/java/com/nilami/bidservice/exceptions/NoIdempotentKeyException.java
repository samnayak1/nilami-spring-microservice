package com.nilami.bidservice.exceptions;

public class NoIdempotentKeyException extends RuntimeException{
        public NoIdempotentKeyException(String message) {
        super(message);
    }
}
