package com.nilami.bidservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidBidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBidException(InvalidBidException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_BID", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(ItemExpiredException.class)
    public ResponseEntity<ErrorResponse> handleItemExpiredException(ItemExpiredException ex) {
        ErrorResponse error = new ErrorResponse("ITEM_EXPIRED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(PaymentException.class)
     public ResponseEntity<PaymentErrorResponse> handlePaymentException(PaymentException ex) {
    PaymentErrorResponse error = PaymentErrorResponse.builder()
            .code("PAYMENT_FAILURE")
            .message(ex.getMessage())
            .userId(ex.getUserId())
            .placedBidAmount(ex.getPlacedBidAmount())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
    ErrorResponse error = new ErrorResponse("RUNTIME_ERROR", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
}

}
