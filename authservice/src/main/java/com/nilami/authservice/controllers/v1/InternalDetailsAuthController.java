package com.nilami.authservice.controllers.v1;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nilami.authservice.controllers.requestTypes.BalanceRequest;
import com.nilami.authservice.controllers.requestTypes.GetMultipleUserDetailsRequest;
import com.nilami.authservice.dto.ApiResponse;
import com.nilami.authservice.dto.UserDTO;
import com.nilami.authservice.exceptions.InsufficientBalanceException;
import com.nilami.authservice.exceptions.UserDoesNotExistException;
import com.nilami.authservice.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/internal/auth")
@RequiredArgsConstructor
public class InternalDetailsAuthController {

    
    private final UserService userService;
   
    @GetMapping("/details")
    public ResponseEntity<ApiResponse> getUserDetails(@RequestParam String userId) {
        try {
            UserDTO userShown = userService.getUserDetails(userId);

            ApiResponse response = new ApiResponse("Success", userShown);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse response = new ApiResponse("An error occurred: " + ex.getMessage() + ex, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    } 

    @PostMapping("/many/details")
public ResponseEntity<ApiResponse> getMutipleUserDetails(@RequestBody GetMultipleUserDetailsRequest request) {
    try {
        List<UserDTO> users = userService.getUsersDetailsByIds(request.getUserIds());

        ApiResponse response = new ApiResponse("Success", users);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (Exception ex) {
        ApiResponse response = new ApiResponse("An error occurred: " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

   @PostMapping("/balance/subtract")
public ResponseEntity<ApiResponse> subtractBankBalanceFromUser(
        @RequestBody BalanceRequest request) {
     try {
        String userId=request.getUserId();
        BigDecimal price=request.getPrice();

        Boolean result = userService.subtractBankBalanceFromUser(userId, price);
        
        ApiResponse response = ApiResponse.builder()
                .message("Balance subtracted successfully")
                .data(result)
                .build();
        
        return ResponseEntity.ok(response);
        
    } catch (UserDoesNotExistException e) {
        ApiResponse response = ApiResponse.builder()
                .message(e.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        
    } catch (InsufficientBalanceException e) {
        ApiResponse response = ApiResponse.builder()
                .message(e.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        
    } catch (Exception e) {
        ApiResponse response = ApiResponse.builder()
                .message("An error occurred while subtracting balance: " + e.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
    
     @PostMapping("/balance/add")
    public ResponseEntity<ApiResponse> addBankBalanceToUser(
        @RequestBody BalanceRequest request){
     try {
        String userId=request.getUserId();
        BigDecimal price=request.getPrice();

        Boolean result = userService.addBankBalanceToUser(userId, price);
        
        ApiResponse response = ApiResponse.builder()
                .message("Balance added successfully")
                .data(result)
                .build();
        
        return ResponseEntity.ok(response);
        
    } catch (UserDoesNotExistException e) {
        ApiResponse response = ApiResponse.builder()
                .message(e.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        
    } catch (InsufficientBalanceException e) {
        ApiResponse response = ApiResponse.builder()
                .message(e.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        
    } catch (Exception e) {
        ApiResponse response = ApiResponse.builder()
                .message("An error occurred while subtracting balance: " + e.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    }

}
