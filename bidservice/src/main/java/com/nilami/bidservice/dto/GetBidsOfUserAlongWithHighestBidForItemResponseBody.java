package com.nilami.bidservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

import java.util.UUID;


public interface GetBidsOfUserAlongWithHighestBidForItemResponseBody  {
    UUID getId();
    UUID getCreatorId();
    UUID getItemId();
    BigDecimal getPrice();
    Instant getCreatedAt();
    Boolean getIsHighestBid();
}