package com.nilami.api_gateway.controllers.v1;

import java.math.BigDecimal;
import java.util.Date;
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
import com.nilami.api_gateway.controllers.requestTypes.RefreshTokenRequest;
import com.nilami.api_gateway.controllers.requestTypes.SignupRequest;
import com.nilami.api_gateway.dto.ApiResponse;
import com.nilami.api_gateway.exceptions.KeycloakClientError;
import com.nilami.api_gateway.models.UserModel;
import com.nilami.api_gateway.services.UserAuthSignupService;
import com.nilami.api_gateway.services.externalClients.AuthClient;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;



@RestController
@Slf4j
@RequestMapping("/api/v1/gateway")
public class GatewayController {

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
            form.add("username", loginRequest.getEmail());
            form.add("password", loginRequest.getPassword());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            System.out.println("response from keycloak login" + response.getBody());
            return ResponseEntity.ok(response.getBody());

        } catch (HttpClientErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of("error", "Keycloak rejected login", "details", e.getResponseBodyAsString()));
        } catch (Exception e) {
          
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error during login", "details", e.getMessage()));
        }
    }

    @PostMapping("/signup")
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(
            name = "authService", 
            fallbackMethod = "authServiceFallback"
    )
    public ResponseEntity<ApiResponse<UserModel>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        String keycloakUserId = null;
        
        log.info("Starting user signup process for email: {}", signupRequest.getEmail());
        log.debug("Signup request details - Name: {}, Age: {}, Gender: {}", 
                  signupRequest.getName(), signupRequest.getAge(), signupRequest.getGender());

        try {
            // Create user in Keycloak
            log.info("Attempting to create user in Keycloak for email: {}", signupRequest.getEmail());
            keycloakUserId = userAuthSignupService.createUser(signupRequest);
            log.info("Successfully created user in Keycloak with ID: {} for email: {}", 
                     keycloakUserId, signupRequest.getEmail());
            
            signupRequest.setId(keycloakUserId);
            
            // Create user in Auth Service
            log.info("Attempting to create user in Auth Service for Keycloak ID: {}", keycloakUserId);
            ApiResponse<UserModel> authResponse = authClient.createUser(signupRequest);
            log.info("Successfully created user in Auth Service for Keycloak ID: {}", keycloakUserId);

            log.info("User signup completed successfully for email: {} with Keycloak ID: {}", 
                     signupRequest.getEmail(), keycloakUserId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "User registered successfully", authResponse.getData()));

        } catch (KeycloakClientError e) {
            log.error("Keycloak user creation failed for email: {}. Error: {}", 
                      signupRequest.getEmail(), e.getMessage(), e);
            
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to create user in Keycloak: " + e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error during signup for email: {}. Keycloak ID: {}. Error: {}", 
                      signupRequest.getEmail(), keycloakUserId, e.getMessage(), e);
            
            return handleRegistrationFailure(keycloakUserId, e);
        }
    }

@SuppressWarnings("rawtypes")
@PostMapping("/refresh")
public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest requestBody) {
    try {
        String refreshToken = requestBody.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse<String>(false,"Refresh token not present", refreshToken));
        }

        String tokenUrl = keycloakRealm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientProps.getClientId());
        form.add("client_secret", clientProps.getClientSecret());
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
        return ResponseEntity.ok(response.getBody());

    } catch (HttpClientErrorException e) {
        return ResponseEntity
                .status(e.getStatusCode())
                .body(Map.of("error", "Keycloak rejected refresh", "details", e.getResponseBodyAsString()));
    } catch (Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unexpected error during refresh", "details", e.getMessage()));
    }
}
    
private ResponseEntity<ApiResponse<UserModel>> handleRegistrationFailure(String keycloakUserId, Exception ex) {
    if (keycloakUserId != null) {
        try {
            userAuthSignupService.deleteUser(keycloakUserId);
            log.info("Rolled back Keycloak user creation for userId: {}", keycloakUserId);
        } catch (Exception rollbackEx) {
            log.error("Failed to rollback Keycloak user creation for userId: {}", keycloakUserId, rollbackEx);
        }
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse<UserModel>(false,"User registration failed: " + ex.getMessage()+ex.toString(), null));
}

 



   
public UserModel authServiceFallback(SignupRequest signupRequest, Throwable t) {
     UserModel fallbackUser = new UserModel();
    fallbackUser.setId(null);
    fallbackUser.setName("Unknown User");
    fallbackUser.setEmail(signupRequest.getEmail());
    fallbackUser.setAge(0);
    fallbackUser.setProfilePicture(null);
    fallbackUser.setBio("Service unavailable, fallback user returned");
    fallbackUser.setGender(null);
    fallbackUser.setBalance(BigDecimal.ZERO);
    fallbackUser.setAddress(null);
    fallbackUser.setRole(null);
    fallbackUser.setCreated(new Date());
    fallbackUser.setUpdated(new Date());
    return fallbackUser;
}

}

