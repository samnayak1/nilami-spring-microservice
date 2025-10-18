package com.nilami.bidservice.services.externalClients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.nilami.bidservice.configs.FeignHeaderForwardingConfig;
import com.nilami.bidservice.controllers.requestTypes.BalanceRequest;
import com.nilami.bidservice.dto.ApiResponse;

@FeignClient(name = "AUTH-SERVICE", url = "${AUTH_SERVICE_HOST}", configuration = FeignHeaderForwardingConfig.class)
public interface UserClient {

    @PostMapping("/api/v1/internal/auth/many/details")
    public ApiResponse getUsersDetailsByIds(List<String> userIds);

    @GetMapping("/api/v1/internal/auth/details")
    public ApiResponse getUserDetails(@RequestParam("userId") String userId);

    
    @PostMapping("/balance/subtract")
       ApiResponse subtractBankBalanceFromUser(
           @RequestBody BalanceRequest request
    );

    @PostMapping("/balance/add")
    ApiResponse addBankBalanceFromUser(
         @RequestBody BalanceRequest request  
    );

}