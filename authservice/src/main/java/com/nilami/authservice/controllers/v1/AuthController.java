package com.nilami.authservice.controllers.v1;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GroupType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.nilami.authservice.configs.CognitoProperties;
import com.nilami.authservice.controllers.requestTypes.LoginRequest;
import com.nilami.authservice.controllers.requestTypes.RefreshTokenRequest;
import com.nilami.authservice.controllers.requestTypes.SignupRequest;
import com.nilami.authservice.controllers.requestTypes.TokenValidationRequest;
import com.nilami.authservice.dto.ApiResponse;
import com.nilami.authservice.dto.LoginResponse;
import com.nilami.authservice.dto.RefreshTokenResponse;
import com.nilami.authservice.dto.TokenValidationResponse;
import com.nilami.authservice.dto.UserDTO;
import com.nilami.authservice.models.UserInfo;
import com.nilami.authservice.models.UserModel;
import com.nilami.authservice.services.UserAuthSignupService;
import com.nilami.authservice.services.UserService;
import com.nilami.authservice.services.UserSignupService;

import jakarta.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserSignupService userSignupService;
    private final UserAuthSignupService userAuthSignupService;
    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoProperties cognitoProps;
    private final UserService userService;

    public AuthController(UserSignupService userSignupService,
            UserAuthSignupService userAuthSignupService,
            CognitoIdentityProviderClient cognitoClient,
            CognitoProperties cognitoProps,
            UserService userService
        ) {
        this.userSignupService = userSignupService;
        this.userAuthSignupService = userAuthSignupService;
        this.cognitoClient = cognitoClient;
        this.cognitoProps = cognitoProps;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.debug("Login started for user" + loginRequest.getEmail());
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", loginRequest.getEmail());
            authParameters.put("PASSWORD", loginRequest.getPassword());

            authParameters.put("SECRET_HASH", calculateSecretHash(loginRequest.getEmail()));

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                    .userPoolId(cognitoProps.getUserPoolId())
                    .clientId(cognitoProps.getClientId())
                    .authParameters(authParameters)
                    .build();

            AdminInitiateAuthResponse response = cognitoClient.adminInitiateAuth(authRequest);

            AuthenticationResultType authResult = response.authenticationResult();
            LoginResponse loginResponse = new LoginResponse(
                    authResult.accessToken(),
                    authResult.idToken(),
                    authResult.refreshToken(),
                    authResult.tokenType(),
                    authResult.expiresIn());
            log.debug("auth response for user" + loginRequest.getEmail() + loginResponse);
            return ResponseEntity.ok(loginResponse);

        } catch (CognitoIdentityProviderException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Cognito rejected login", "details", e.awsErrorDetails().errorMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserModel>> signup(@Valid @RequestBody SignupRequest request) {
        String cognitoUserId = null;
        try {
            // in congnitop
            cognitoUserId = userAuthSignupService.createUser(request);
            request.setId(cognitoUserId);

            // local database
            UserModel authResponse = userSignupService.signupUser(request);

            return ResponseEntity.ok(new ApiResponse<>(true, "User registered successfully", authResponse));

        } catch (Exception ex) {
            log.error("Signup failed for {}: {}", request.getEmail(), ex.getMessage());
            if (cognitoUserId != null)
                userAuthSignupService.deleteUser(cognitoUserId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Registration failed: " + ex.getMessage(), null));
        }
    }

 @PostMapping("/refresh")
public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest requestBody) {
    try {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("REFRESH_TOKEN", requestBody.getRefreshToken());
        authParams.put("SECRET_HASH", calculateSecretHash(requestBody.getUserId()));
        
        AdminInitiateAuthRequest refreshRequest = AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .userPoolId(cognitoProps.getUserPoolId())
                .clientId(cognitoProps.getClientId())
                .authParameters(authParams)
                .build();

        AdminInitiateAuthResponse response = cognitoClient.adminInitiateAuth(refreshRequest);
        AuthenticationResultType authResult = response.authenticationResult();
        
       
        RefreshTokenResponse refreshResponse = new RefreshTokenResponse(
            authResult.accessToken(),
            authResult.idToken(),
            authResult.tokenType(),
            authResult.expiresIn()
        );
        
        return ResponseEntity.ok(refreshResponse);

    } catch (CognitoIdentityProviderException e) {
        log.error("Cognito refresh token failed", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "error", "aws response failed",
                    "message", e.awsErrorDetails().errorMessage()
                ));
    } catch (Exception e) {
        log.error("Unexpected error during token refresh", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "token failed",
                    "message", e.getMessage()
                ));
    }
}
@PostMapping("/validate-token")
public ResponseEntity<?> validateToken(@RequestBody TokenValidationRequest request) {
    try {
        GetUserRequest getUserRequest = GetUserRequest.builder()
                .accessToken(request.getToken())
                .build();

        GetUserResponse response = cognitoClient.getUser(getUserRequest);


        AdminListGroupsForUserRequest groupsRequest = AdminListGroupsForUserRequest.builder()
                .username(response.username())
                .userPoolId(cognitoProps.getUserPoolId())
                .build();

        AdminListGroupsForUserResponse groupsResponse = cognitoClient.adminListGroupsForUser(groupsRequest);

        List<String> roles = groupsResponse.groups().stream()
                .map(GroupType::groupName)
                .collect(Collectors.toList());

        UserDTO user=  userService.getUserDetails(response.username());
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(response.username());
        userInfo.setUsername(user.getName());
        userInfo.setEmail(response.userAttributes().stream()
                .filter(a -> a.name().equals("email"))
                .findFirst()
                .map(AttributeType::value)
                .orElse(null));
        userInfo.setRoles(roles);
   


        return ResponseEntity.ok(new TokenValidationResponse(true, "Token is valid", userInfo));

    } catch (Exception e) {
        return ResponseEntity.ok(new TokenValidationResponse(false, "Token is invalid", null));
    }
}

    private String calculateSecretHash(String userName) {
        String clientId = cognitoProps.getClientId();
        String clientSecret = cognitoProps.getClientSecret();

        SecretKeySpec signingKey = new SecretKeySpec(
                clientSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(clientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating secret hash", e);
        }
    }
}
