package com.nilami.api_gateway.controllers.v1;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


import com.nilami.api_gateway.configs.KeycloakClientProperties;
import com.nilami.api_gateway.controllers.requestTypes.LoginRequest;
import com.nilami.api_gateway.controllers.requestTypes.SignupRequest;
import com.nilami.api_gateway.dto.ApiResponse;
import com.nilami.api_gateway.exceptions.KeycloakClientError;
import com.nilami.api_gateway.models.UserModel;
import com.nilami.api_gateway.services.UserAuthSignupService;
import com.nilami.api_gateway.services.externalClients.AuthClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayController {
    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final KeycloakClientProperties clientProps;
    private UserAuthSignupService userAuthSignupService;
    private AuthClient authClient;
    

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String keycloakRealm;

    public GatewayController(
            KeycloakClientProperties clientProps,
            UserAuthSignupService userAuthSignupService,
            AuthClient authClient) {
        this.clientProps = clientProps;
        this.userAuthSignupService = userAuthSignupService;
        this.authClient = authClient;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testController() {
        return ResponseEntity.ok("Hello from gateway");
    }

    @SuppressWarnings("rawtypes")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("Login started");
        try {
            String tokenUrl = keycloakRealm+"/protocol/openid-connect/token";

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", clientProps.getClientId());
            form.add("client_secret", clientProps.getClientSecret());
            form.add("grant_type", "password");
            form.add("username", loginRequest.getUserName());
            form.add("password", loginRequest.getPassword());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            System.out.println("response from keycloak login" + response.getBody());
            return ResponseEntity.ok(response.getBody());

        } catch (HttpClientErrorException e) {
            // Covers 4xx errors from Keycloak (bad creds, unauthorized, etc.)
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of("error", "Keycloak rejected login", "details", e.getResponseBodyAsString()));
        } catch (Exception e) {
            // Any other unexpected error
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error during login", "details", e.getMessage()));
        }
    }

@PostMapping("/signup")
public ResponseEntity<ApiResponse> signup(@RequestBody SignupRequest signupRequest) {
    String keycloakUserId = null;

    try {

        keycloakUserId = userAuthSignupService.createUser(signupRequest);

 
        UserModel authResponse = authClient.createUser(signupRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("User registered successfully", authResponse));

    } catch (KeycloakClientError e) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse("Failed to create user in Keycloak: " + e.getMessage(), null));

    } catch (Exception e) {
        return handleRegistrationFailure(keycloakUserId, e);
    }
}


private ResponseEntity<ApiResponse> handleRegistrationFailure(String keycloakUserId, Exception ex) {
    if (keycloakUserId != null) {
        try {
            userAuthSignupService.deleteUser(keycloakUserId);
            log.info("Rolled back Keycloak user creation for userId: {}", keycloakUserId);
        } catch (Exception rollbackEx) {
            log.error("Failed to rollback Keycloak user creation for userId: {}", keycloakUserId, rollbackEx);
        }
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse("User registration failed: " + ex.getMessage(), null));
}

 


}

