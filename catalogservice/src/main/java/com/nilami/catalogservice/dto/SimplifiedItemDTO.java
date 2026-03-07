package com.nilami.catalogservice.dto;

import java.math.BigDecimal;
import java.util.Date;
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
public class SimplifiedItemDTO {
    private UUID id;

    private String title;

    private BigDecimal basePrice;

    private String brand;

    private Date expiryTime;

    private boolean deleted;
   
    private String location;
    
}
