package com.nilami.api_gateway.services.externalClients;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nilami.api_gateway.controllers.requestTypes.SignupRequest;
import com.nilami.api_gateway.dto.ApiResponse;


@FeignClient(
    name = "AUTH-SERVICE",
    url="${AUTH_SERVICE_HOST}"
)
public interface AuthClient {

    @PostMapping("/api/v1/auth/signup")
    ApiResponse createUser(@RequestBody SignupRequest signupRequest);

    
}