package com.nilami.bidservice.controllers.requestTypes;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PlaceBidRequest {
    
      @NotBlank(message = "The itemId is required.")
      private String itemId;
      
      @NotBlank(message = "The price is required.")
      private BigDecimal price;

}
