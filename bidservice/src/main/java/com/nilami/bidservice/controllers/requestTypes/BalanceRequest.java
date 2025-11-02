package com.nilami.bidservice.controllers.requestTypes;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class BalanceRequest {
    private String userId;
    private BigDecimal price;
    
}