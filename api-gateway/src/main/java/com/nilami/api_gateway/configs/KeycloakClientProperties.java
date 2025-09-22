package com.nilami.api_gateway.configs;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

@Component
public class KeycloakClientProperties {
    @Value("${KEYCLOAK_CLIENT_ID}")
    private String clientId;

    @Value("${KEYCLOAK_CLIENT_SECRET}")
    private String clientSecret;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}