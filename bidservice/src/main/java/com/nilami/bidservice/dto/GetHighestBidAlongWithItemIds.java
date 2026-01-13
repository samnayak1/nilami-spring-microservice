package com.nilami.bidservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public interface GetHighestBidAlongWithItemIds {
    UUID getItemId();
    BigDecimal getHighestBidPrice();
}
