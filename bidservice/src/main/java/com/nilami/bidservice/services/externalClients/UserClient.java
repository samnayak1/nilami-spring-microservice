package com.nilami.bidservice.services.externalClients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.nilami.bidservice.configs.FeignHeaderForwardingConfig;

import com.nilami.bidservice.controllers.requestTypes.BalanceReservationRequest;
import com.nilami.bidservice.dto.ApiResponse;
import com.nilami.bidservice.dto.BalanceReservationResponse;
import com.nilami.bidservice.dto.UserDTO;

@FeignClient(
    name = "AUTH-SERVICE",
     url = "${AUTH_SERVICE_HOST}",
     configuration = FeignHeaderForwardingConfig.class,
       path = "/api/v1/internal/auth")
public interface UserClient {

    @PostMapping("/many/details")
    public ApiResponse<List<UserDTO>> getUsersDetailsByIds(List<String> userIds);

    @GetMapping("/details")
    public ApiResponse<UserDTO> getUserDetails(@RequestParam("userId") String userId);

 
        @PostMapping("/balance/reserve")
    ApiResponse<BalanceReservationResponse> reserveBalance(
            @RequestBody BalanceReservationRequest request
    );

    @PostMapping("/balance/commit/{reservationId}")
  ApiResponse<Void> commitBalanceReservation(
            @PathVariable("reservationId") String reservationId
    );

    @PostMapping("/balance/cancel/{reservationId}")
    ApiResponse<Void> cancelBalanceReservation(
            @PathVariable("reservationId") String reservationId
    );

}