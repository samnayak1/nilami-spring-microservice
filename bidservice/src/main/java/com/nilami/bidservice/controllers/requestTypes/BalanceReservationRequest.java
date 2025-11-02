package com.nilami.bidservice.controllers.requestTypes;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BalanceReservationRequest {
    private String userId;
    private BigDecimal amount;
    private String idempotentKey; 
}
