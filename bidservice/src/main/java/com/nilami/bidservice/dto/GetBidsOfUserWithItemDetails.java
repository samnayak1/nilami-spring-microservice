package com.nilami.bidservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class GetBidsOfUserWithItemDetails {
    private UUID id;
    private UUID creatorId;
    private UUID itemId;
    private BigDecimal price;
    private Instant createdAt;
    private Boolean isHighestBid; 

    private String title;

    private BigDecimal basePrice;

    private String brand;

    private Date expiryTime;

    private boolean deleted;
}
