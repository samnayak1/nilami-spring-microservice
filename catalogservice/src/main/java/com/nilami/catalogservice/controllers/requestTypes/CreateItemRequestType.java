package com.nilami.catalogservice.controllers.requestTypes;

import java.math.BigDecimal;
import java.util.Date;


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
     @NotBlank(message = "The title is required.")
     private String title;
 @NotBlank(message = "The description is required.")
    private String description;
 @NotBlank(message = "The base price is required.")
    private BigDecimal basePrice;
 @NotBlank(message = "The brand is required.")
    private String brand;

//  @NotBlank(message = "Attach atleast one picture")
//     private List<String> pictureIds;
 @NotBlank(message = "category is required")
    private String categoryId;
@NotBlank(message = "set an expiry time")
    private Date expiryTime;
}
