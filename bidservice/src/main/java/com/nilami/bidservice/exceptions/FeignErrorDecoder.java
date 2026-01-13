package com.nilami.bidservice.exceptions;

import java.io.IOException;

import org.springframework.stereotype.Component;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    

    private final ErrorDecoder defaultDecoder = new Default();

       @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign client error: method={}, status={}", methodKey, response.status());
        
        String errorMessage = getErrorMessage(response);
        
        switch (response.status()) {
            case 400:
                if (methodKey.contains("reserveBalance") || methodKey.contains("commitBalanceReservation")) {
                    return new BidPlacementException("Invalid balance operation: " + errorMessage);
                }
                return new BidPlacementException("Bad request: " + errorMessage);
                
            case 404:
                if (methodKey.contains("getItem") || methodKey.contains("getItemDetails")) {
                    return new ItemNotFoundException("Item not found: " + errorMessage);
                }
                if (methodKey.contains("getUserDetails")) {
                    return new UserNotFoundException("User not found: " + errorMessage);
                }
                if (methodKey.contains("commitBalanceReservation")) {
                    return new BidPlacementException("Reservation not found: " + errorMessage);
                }
                 if (methodKey.contains("addBalanceToUser")) {
                    log.error("Saga completed with failed compensations"+errorMessage);
                    return new BidPlacementException("Error adding balance back to user's account" + errorMessage);
                }
                if(methodKey.contains("cancelBalanceReservation")){
                       log.error("Saga completed with failed compensations"+errorMessage);
                    return new BidPlacementException("Error cancelleling reservation" + errorMessage);
                }


                



                
                return new BidPlacementException("Resource not found: " + errorMessage);
                
            case 409:
                return new BidPlacementException("Conflict: " + errorMessage);
                
            case 500:
            case 503:
                return new BidPlacementException("Service unavailable: " + errorMessage);
                
            default:
                return defaultDecoder.decode(methodKey, response);
        }
    }
    
    private String getErrorMessage(Response response) {
        try {
            if (response.body() != null) {
                return new String(response.body().asInputStream().readAllBytes());
            }
        } catch (IOException e) {
            log.error("Error reading response body", e);
        }
        return "Unknown error";
    }

}