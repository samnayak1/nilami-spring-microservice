package com.nilami.authservice.controllers.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nilami.authservice.controllers.requestTypes.SignupRequest;
import com.nilami.authservice.dto.ApiResponse;
import com.nilami.authservice.services.UserSignupService;

@RestController
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
    public ResponseEntity<ApiResponse> signup(@RequestBody SignupRequest request) {
       

        String savedUserId=userSignupService.signupUser(request);

        ApiResponse response = new ApiResponse("User registered successfully", savedUserId);
        return ResponseEntity.ok(response);
    }
}
