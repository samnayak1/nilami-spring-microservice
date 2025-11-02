package com.nilami.authservice.controllers.v1;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nilami.authservice.controllers.requestTypes.BalanceRequest;
import com.nilami.authservice.controllers.requestTypes.BalanceReservationRequest;
import com.nilami.authservice.dto.ApiResponse;
import com.nilami.authservice.dto.BalanceReservationResponse;
import com.nilami.authservice.dto.UserDTO;
import com.nilami.authservice.exceptions.InsufficientBalanceException;
import com.nilami.authservice.exceptions.UserDoesNotExistException;
import com.nilami.authservice.services.BalanceService;
import com.nilami.authservice.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/internal/auth")
@RequiredArgsConstructor
public class InternalDetailsAuthController {

    
    private final UserService userService;

    private final BalanceService balanceService;
   
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<UserDTO>> getUserDetails(@RequestParam String userId) {
        try {
            UserDTO userShown = userService.getUserDetails(userId);

            ApiResponse<UserDTO> response = new ApiResponse<UserDTO>(true,"Success", userShown);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<UserDTO> response = new ApiResponse<UserDTO>(false,"An error occurred: " + ex.getMessage() + ex, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    } 



   @PostMapping("/balance/subtract")
public ResponseEntity<ApiResponse<Boolean>> subtractBankBalanceFromUser(
        @RequestBody BalanceRequest request) {
     try {
        String userId=request.getUserId();
        BigDecimal price=request.getPrice();

        Boolean result = userService.subtractBankBalanceFromUser(userId, price);
        
       ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
        .success(true)
        .message("Balance subtracted successfully")
        .data(result)
        .build();
        return ResponseEntity.ok(response);
        
    } catch (UserDoesNotExistException e) {
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(false)
                .message(e.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        
    } catch (InsufficientBalanceException e) {
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(false)
                .message(e.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        
    } catch (Exception e) {
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(false)
                .message("An error occurred while subtracting balance: " + e.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
    
     @PostMapping("/balance/add")
    public ResponseEntity<ApiResponse<Boolean>> addBankBalanceToUser(
        @RequestBody BalanceRequest request){
     try {
        String userId=request.getUserId();
        BigDecimal price=request.getPrice();

        Boolean result = userService.addBankBalanceToUser(userId, price);
        
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(true)
                .message("Balance added successfully")
                .data(result)
                .build();
        
        return ResponseEntity.ok(response);
        
    } catch (UserDoesNotExistException e) {
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                 .success(false)
                .message(e.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        
    } catch (InsufficientBalanceException e) {
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(false)
                .message(e.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        
    } catch (Exception e) {
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(false)
                .message("An error occurred while subtracting balance: " + e.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    }


@PostMapping("/balance/reserve")
public ResponseEntity<ApiResponse<BalanceReservationResponse>> reserveBalance(
        @Valid @RequestBody BalanceReservationRequest request) {
    try {
        BalanceReservationResponse response = balanceService.reserveBalance(request);

        return ResponseEntity.ok(ApiResponse.<BalanceReservationResponse>builder()
                .success(true)
                .message("Reservation request success")
                .data(response)
                .build());
    } catch (Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<BalanceReservationResponse>builder()
                        .success(false)
                        .message("Reservation failed: " + e.getMessage())
                        .data(null)
                        .build());
    }
}

@PostMapping("/balance/commit/{reservationId}")
public ResponseEntity<ApiResponse<Void>> commitBalanceReservation(
        @PathVariable String reservationId) {
    try {
        balanceService.commitBalanceReservation(reservationId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Reservation commit request success")
                .data(null)
                .build());
    } catch (Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Commit failed: " + e.getMessage())
                        .data(null)
                        .build());
    }
}

@PostMapping("/balance/cancel/{reservationId}")
public ResponseEntity<ApiResponse<Void>> cancelBalanceReservation(
        @PathVariable String reservationId) {
    try {
        balanceService.cancelBalanceReservation(UUID.fromString(reservationId));

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Reservation cancellation request success")
                .data(null)
                .build());
    } catch (Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Cancellation failed: " + e.getMessage())
                        .data(null)
                        .build());
    }
}

}
