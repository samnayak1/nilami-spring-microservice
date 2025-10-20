package com.nilami.bidservice.exceptions;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentErrorResponse {
    private String code;
    private String message;
    private String userId;
    private BigDecimal placedBidAmount;
}