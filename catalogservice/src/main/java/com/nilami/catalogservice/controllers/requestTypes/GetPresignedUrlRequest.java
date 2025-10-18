package com.nilami.catalogservice.controllers.requestTypes;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetPresignedUrlRequest {
    
    @NotBlank(message = "The fileName is required.")
    private String fileName;

    @NotBlank(message = "The objectId is required.")
    private String objectId;
}
