package com.nilami.authservice.controllers.v1;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

import com.nilami.authservice.configs.KeycloakClientProperties;
import com.nilami.authservice.controllers.requestTypes.LoginRequest;
import com.nilami.authservice.controllers.requestTypes.RefreshTokenRequest;
import com.nilami.authservice.controllers.requestTypes.SignupRequest;
import com.nilami.authservice.controllers.requestTypes.TokenValidationRequest;
import com.nilami.authservice.dto.ApiResponse;
import com.nilami.authservice.dto.TokenValidationResponse;

import com.nilami.authservice.models.UserInfo;
import com.nilami.authservice.models.UserModel;
import com.nilami.authservice.services.UserAuthSignupService;
import com.nilami.authservice.services.UserSignupService;

import jakarta.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserSignupService userSignupService;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String keycloakRealm;


    private final RestTemplate restTemplate = new RestTemplate();
    private final KeycloakClientProperties clientProps;
    private UserAuthSignupService userAuthSignupService;

    public AuthController(UserSignupService userSignupService,
            KeycloakClientProperties clientProps,
            UserAuthSignupService userAuthSignupService
    ) {
        this.userSignupService = userSignupService;
        this.userAuthSignupService=userAuthSignupService;
        this.clientProps=clientProps;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testController() {
        return ResponseEntity.ok("Hello");
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
    public ResponseEntity<ApiResponse<UserModel>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Received signup request for email: {}", request.getEmail());
        log.debug("Signup request details - Name: {}, Age: {}, Gender: {}",
                request.getName(), request.getAge(), request.getGender());
        String keycloakUserId = null;
        try {
            // Create user in Keycloak
            log.info("Attempting to create user in Keycloak for email: {}", request.getEmail());
            keycloakUserId = userAuthSignupService.createUser(request);
            log.info("Successfully created user in Keycloak with ID: {} for email: {}", 
                     keycloakUserId, request.getEmail());
            
            request.setId(keycloakUserId);
            
            // Create user in Auth Service
            log.info("Attempting to create user in Auth Service for Keycloak ID: {}", keycloakUserId);
            UserModel authResponse = userSignupService.signupUser(request);
            log.info("Successfully created user in Auth Service for Keycloak ID: {}", keycloakUserId,authResponse.getId());

            log.info("User signup completed successfully for email: {} with Keycloak ID: {}", 
                     request.getEmail(), keycloakUserId);
        
        
    
            
            UserModel savedUser = userSignupService.signupUser(request);

            log.info("User registered successfully with ID: {} and email: {}",
                    savedUser.getId(), savedUser.getEmail());

            ApiResponse<UserModel> response = new ApiResponse<>(
                    true,
                    "User registered successfully",
                    savedUser);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            log.warn("Invalid signup request for email: {}. Reason: {}",
                    request.getEmail(), ex.getMessage());

            ApiResponse<UserModel> response = new ApiResponse<>(
                    false,
                    "Invalid request: " + ex.getMessage(),
                    null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception ex) {
            log.error("Unexpected error during signup for email: {}. Error: {}",
                    request.getEmail(), ex.getMessage(), ex);

     
            log.error("Unexpected error during signup for email: {}. Keycloak ID: {}. Error: {}", 
                      request.getEmail(), keycloakUserId, ex.getMessage(), ex);
            
            return handleRegistrationFailure(keycloakUserId, ex);
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

   @SuppressWarnings({ "rawtypes", "unchecked", "null" })
@PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestBody TokenValidationRequest request) {
        
        try {
            String token = request.getToken();
            
            if (token == null || token.isBlank()) {
                return ResponseEntity.badRequest()
                    .body(new TokenValidationResponse(false, "Token is required", null));
            }
            

            String introspectionUrl = keycloakRealm + "/protocol/openid-connect/token/introspect";
            
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
             form.add("client_id", clientProps.getClientId());
             form.add("client_secret", clientProps.getClientSecret());
             form.add("token", token);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                introspectionUrl, 
                requestEntity, 
                Map.class
            );
            
            Map<String, Object> introspectionResult = response.getBody();
            
            // Check if token is active
            Boolean active = (Boolean) introspectionResult.get("active");
            
            if (Boolean.TRUE.equals(active)) {
                // Extract user information
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId((String) introspectionResult.get("sub"));
                userInfo.setUsername((String) introspectionResult.get("preferred_username"));
                userInfo.setEmail((String) introspectionResult.get("email"));
                
                // Extract roles
                Map<String, Object> realmAccess = (Map<String, Object>) introspectionResult.get("realm_access");
                if (realmAccess != null) {
                    List<String> roles = (List<String>) realmAccess.get("roles");
                    userInfo.setRoles(roles != null ? roles : new ArrayList<>());
                }
                
                // Extract token expiry
                Long exp = ((Number) introspectionResult.get("exp")).longValue();
                userInfo.setTokenExpiry(Instant.ofEpochSecond(exp));
                
                return ResponseEntity.ok(
                    new TokenValidationResponse(true, "Token is valid", userInfo)
                );
            } else {
                return ResponseEntity.ok(
                    new TokenValidationResponse(false, "Token is not active", null)
                );
            }
            
        } catch (HttpClientErrorException e) {
            log.error("Keycloak introspection failed: {}", e.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new TokenValidationResponse(false, "Token validation failed", null));
                
        } catch (Exception e) {
            log.error("Unexpected error during token validation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new TokenValidationResponse(false, "Internal error", null));
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

}
