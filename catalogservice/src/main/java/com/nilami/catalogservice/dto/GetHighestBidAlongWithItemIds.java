package com.nilami.catalogservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetHighestBidAlongWithItemIds {
    private UUID itemId;
    private UUID userId;
    private BigDecimal highestBidPrice;
}
