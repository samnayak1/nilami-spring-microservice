package com.nilami.api_gateway.services.externalClients;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nilami.api_gateway.controllers.requestTypes.SignupRequest;
import com.nilami.api_gateway.models.UserModel;

@FeignClient(name = "auth-service", url = "")
public interface AuthClient {
    @PostMapping("/api/v1/auth/signup")
    UserModel createUser(@RequestBody SignupRequest signupRequest);
}

