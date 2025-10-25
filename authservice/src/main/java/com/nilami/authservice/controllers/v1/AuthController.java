package com.nilami.authservice.controllers.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.nilami.authservice.controllers.requestTypes.SignupRequest;
import com.nilami.authservice.dto.ApiResponse;

import com.nilami.authservice.models.UserModel;

import com.nilami.authservice.services.UserSignupService;

import jakarta.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserSignupService userSignupService;

    public AuthController(UserSignupService userSignupService) {
        this.userSignupService = userSignupService;

    }

    @GetMapping("/test")
    public ResponseEntity<String> testController() {
        return ResponseEntity.ok("Hello");
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserModel>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Received signup request for email: {}", request.getEmail());
        log.debug("Signup request details - Name: {}, Age: {}, Gender: {}",
                request.getName(), request.getAge(), request.getGender());

        try {
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

            ApiResponse<UserModel> response = new ApiResponse<>(
                    false,
                    "An error occurred: " + ex.getMessage(),
                    null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
