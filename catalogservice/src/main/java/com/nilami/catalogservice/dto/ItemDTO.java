package com.nilami.catalogservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;



import com.nilami.catalogservice.models.Item;
import com.nilami.catalogservice.services.serviceAbstractions.FileUploadService;

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

    public static ItemDTO toItemDTO(Item item,FileUploadService fileService) {
        return ItemDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .basePrice(item.getBasePrice())
                .brand(item.getBrand())
                .creatorUserId(item.getCreatorUserId())
                .pictureIds(item.getPictureIds().stream()
                        .map(fileService::generateDownloadPresignedUrl) // Map each picture key to a presigned URL
                        .map(URL::toString) // Convert URL to String if needed
                        .collect(Collectors.toList()))
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .expiryTime(item.getExpiryTime())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .deleted(item.isDeleted())
                .build();
    }
}
