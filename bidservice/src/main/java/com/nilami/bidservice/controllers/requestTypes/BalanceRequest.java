package com.nilami.bidservice.controllers.requestTypes;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class BalanceRequest {
    private String userId;
    private BigDecimal price;
    
}