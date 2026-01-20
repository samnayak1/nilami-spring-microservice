package com.nilami.bidservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Setter
public class BidEventMessageQueuePayload {

    private UUID itemId;
    private UUID bidId;
    private BigDecimal amount;
    private UUID userId;
    private Instant timestamp;
}