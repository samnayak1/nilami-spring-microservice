package com.nilami.bidservice.dto;

import java.math.BigDecimal;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class BidEventMessageQueuePayload {
    
    UUID itemId;
    UUID bidId;
    BigDecimal amount;
    UUID userId;
    String timestamp;

}
