package com.nilami.bidservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class BidDTO {
       private UUID id;

       private Instant created;

       private UUID itemId;

       private String itemName;

       private UUID creatorId;

       private String creatorName;

       private BigDecimal price;

   }