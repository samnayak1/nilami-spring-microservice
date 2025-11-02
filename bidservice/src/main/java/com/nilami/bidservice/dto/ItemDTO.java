package com.nilami.bidservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDTO {


    private UUID id;

    private String title;

    private String description;

    private BigDecimal basePrice;

    private String brand;

    private UUID creatorUserId;

    private List<String> pictureIds;

    private UUID categoryId;

    private Date expiryTime;

    private Instant createdAt;

    private Instant updatedAt;

    private boolean deleted;
}