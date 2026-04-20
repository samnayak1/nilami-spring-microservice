package com.nilami.catalogservice.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public interface SimplifiedItemProjection {
    UUID getId();
    String getTitle();
    BigDecimal getBasePrice();
    String getBrand();
    Date getExpiryTime();
    Boolean getDeleted();
    String getLocation();
}