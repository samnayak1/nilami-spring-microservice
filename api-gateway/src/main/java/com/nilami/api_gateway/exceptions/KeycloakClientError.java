package com.nilami.api_gateway.exceptions;


public class KeycloakClientError extends RuntimeException {
    public KeycloakClientError(String message) {
        super(message);
    }
     public KeycloakClientError(String message,Throwable ex) {
        super(message,ex);
    }
    }
