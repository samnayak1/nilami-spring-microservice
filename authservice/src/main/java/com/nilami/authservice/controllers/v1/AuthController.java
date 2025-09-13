package com.nilami.authservice.controllers.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
      @GetMapping("/test")
    public ResponseEntity<String> testController() {
        return ResponseEntity.ok("Hello");
    }
}
