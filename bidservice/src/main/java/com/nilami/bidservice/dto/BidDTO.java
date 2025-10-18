package com.nilami.bidservice.dto;

import java.math.BigDecimal;

import java.util.Date;


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
       private String id;

       private Date created;

       private String itemId;

       private String itemName;

       private String creatorId;

       private String creatorName;

       private BigDecimal price;

   }