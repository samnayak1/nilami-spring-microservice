package com.nilami.bidservice.services.externalClients.fallback;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nilami.bidservice.controllers.requestTypes.BalanceReservationRequest;
import com.nilami.bidservice.dto.ApiResponse;
import com.nilami.bidservice.dto.BalanceReservationResponse;
import com.nilami.bidservice.dto.Roles;
import com.nilami.bidservice.dto.UserDTO;
import com.nilami.bidservice.services.externalClients.UserClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserClientFallback implements  UserClient{


    @Override
    public ApiResponse<UserDTO> getUserDetails(String userId) {

     log.warn("User Client api is failing for getUserDetails at "+Instant.now());
             UserDTO user=UserDTO.builder()
             .id(UUID.fromString(userId))
             .age(0)
             .bio("")
             .email("")
             .name("")
             .balance(BigDecimal.ZERO)
             .profilePicture("")
             .role(Roles.CUSTOMER)
              
             .build();

        return new ApiResponse<UserDTO>(false, "user not found - user client is down.", user);
                                 
    }

    @Override
    public ApiResponse<BalanceReservationResponse> reserveBalance(BalanceReservationRequest request) {
         log.warn("User Client api is failing for reserveBalance at "+Instant.now());
        return new ApiResponse<BalanceReservationResponse>(false,"user client down",new BalanceReservationResponse("","","failed - user client failed to hit"));
    }

    @Override
    public ApiResponse<Void> commitBalanceReservation(String reservationId) {
         log.warn("User Client api is failing for commitBalanceReservation at "+Instant.now());
         
         return new ApiResponse<Void>(false, reservationId, null);
    }

    @Override
    public ApiResponse<Void> cancelBalanceReservation(String reservationId) {
         log.warn("User Client api is failing for cancelBalanceReservation at "+Instant.now());
         return new ApiResponse<Void>(false, reservationId, null);
    }
    


}
