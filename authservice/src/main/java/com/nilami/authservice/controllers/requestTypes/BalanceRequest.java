package com.nilami.authservice.controllers.requestTypes;

import java.math.BigDecimal;

import lombok.Getter;

@Getter

public class BalanceRequest {
    private String userId;
    private BigDecimal price;
    
}