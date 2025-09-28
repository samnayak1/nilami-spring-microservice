package com.nilami.api_gateway.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(KeycloakClientError.class)
    public ResponseEntity<KeycloakClientError> handleKeycloakClientError(KeycloakClientError ex) {
        KeycloakClientError error = new KeycloakClientError("Keycloak Client",ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
