package com.nilami.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.nilami.catalogservice.models.Item;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDTO {

    private UUID id;

    private String title;

    private String description;

    private BigDecimal basePrice;

    private String brand;

    private String creatorUserId;

    private List<String> pictureIds;

    private UUID categoryId;

    private Date expiryTime;

    private Instant createdAt;

    private Instant updatedAt;

    private boolean deleted;

    public static ItemDTO toItemDTO(Item item) {
        return ItemDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .basePrice(item.getBasePrice())
                .brand(item.getBrand())
                .creatorUserId(item.getCreatorUserId())
                .pictureIds(item.getPictureIds())
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .expiryTime(item.getExpiryTime())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .deleted(item.isDeleted())
                .build();
    }
}
