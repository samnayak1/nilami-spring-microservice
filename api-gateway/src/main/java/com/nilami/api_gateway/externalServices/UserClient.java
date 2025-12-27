package com.nilami.api_gateway.externalServices;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestBody;

import com.nilami.dto.TokenValidationRequest;
import com.nilami.dto.TokenValidationResponse;

@FeignClient(name = "AUTH-SERVICE", path = "/api/v1/auth")
public interface UserClient {

    @GetMapping("/validate-token")
    public TokenValidationResponse validateToken(@RequestBody TokenValidationRequest request);

}
