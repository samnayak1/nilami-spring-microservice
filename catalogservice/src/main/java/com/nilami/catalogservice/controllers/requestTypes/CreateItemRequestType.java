package com.nilami.catalogservice.controllers.requestTypes;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
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
     @NotBlank(message = "The description is required.")
     private String title;
 @NotBlank(message = "The description is required.")
    private String description;
 @NotBlank(message = "The description is required.")
    private BigDecimal basePrice;
 @NotBlank(message = "The description is required.")
    private String brand;


    private List<String> pictureIds;

    private String categoryId;

    private Date expiryTime;
}
