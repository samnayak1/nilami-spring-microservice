package com.nilami.authservice.controllers.requestTypes;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BalanceReservationRequest {
    private String userId;
    private BigDecimal amount;
    private String idempotentKey; 
}