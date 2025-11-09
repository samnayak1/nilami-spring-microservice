package com.nilami.api_gateway.controllers.requestTypes;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class TokenValidationRequest {
    private String token;
}