package com.nilami.bidservice.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class GetHighestBidsRequest {
    
    @NotEmpty(message = "Item IDs cannot be empty")
    @Size(min = 1, max = 30, message = "Number of item IDs must be between 1 and 30")
    private List<UUID> itemIds;

}
