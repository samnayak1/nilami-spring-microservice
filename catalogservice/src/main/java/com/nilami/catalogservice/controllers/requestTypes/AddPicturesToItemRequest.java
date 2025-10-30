package com.nilami.catalogservice.controllers.requestTypes;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter

@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class AddPicturesToItemRequest {
    

    @NotBlank(message = "itemId is not present")
     private String itemId;


      @NotBlank(message = "Attach atleast one picture")
      private List<String> pictureIds;
}
