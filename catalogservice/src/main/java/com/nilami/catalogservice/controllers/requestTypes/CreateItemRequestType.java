package com.nilami.catalogservice.controllers.requestTypes;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class CreateItemRequestType {
     private String title;

    private String description;

    private BigDecimal basePrice;

    private String brand;

    private String creatorUserId;

    private List<String> pictureIds;

    private String categoryId;

    private Date expiryTime;
}
