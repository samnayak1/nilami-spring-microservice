package com.nilami.api_gateway.configs;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class KeycloakClientProperties {
    @Value("${KEYCLOAK_CLIENT_ID}")
    private String clientId;

    @Value("${KEYCLOAK_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${KEYCLOAK_REALM}")
    private String keycloakRealm;

     @Value("${KEYCLOAK_URL}")
    private String serverUrl;

     @Value("${KEYCLOAK_ADMIN}")
    private String adminUser;

    
     @Value("${KEYCLOAK_ADMIN_PASSWORD}")
    private String adminPassword;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getKeyCloakRealm(){
        return keycloakRealm;
    }



 @Bean
Keycloak keycloak() {
     return KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm("master")
            .clientId("admin-cli") 
            .username(adminUser)
            .password(adminPassword)
            .grantType(OAuth2Constants.PASSWORD)
            .build();
}
}