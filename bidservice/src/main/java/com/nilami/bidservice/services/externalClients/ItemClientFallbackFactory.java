package com.nilami.bidservice.services.externalClients;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.nilami.bidservice.dto.ItemDTO;
import com.nilami.bidservice.dto.SimplifiedItemDTO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {

        return new ItemClient() {

            @Override
            public ItemDTO getItem(String itemId) {

                ItemDTO itemToSendIfFallBack = ItemDTO
                        .builder()
                        .basePrice(BigDecimal.valueOf(Double.MAX_VALUE))
                        .expiryTime(Date.from(Instant.now().minusSeconds(36000)))
                        .categoryId(UUID.randomUUID())
                        .deleted(true)
                        .createdAt(Instant.now().minusSeconds(36000))
                        .brand("")
                        .title("N/A")
                        .updatedAt(Instant.now().minusSeconds(36000))
                        .creatorUserId(UUID.randomUUID())
                        .id(UUID.fromString(itemId))
                        .build();

                return itemToSendIfFallBack;

            }

            @Override
            public List<SimplifiedItemDTO> getItemDetails(List<String> itemIds) {
                if (itemIds == null)
                    return List.of();

                return itemIds.stream()
                        .map(id -> {
                            SimplifiedItemDTO dto = new SimplifiedItemDTO();

                            dto.setId(UUID.fromString(id));

                            dto.setTitle("Service Unavailable");
                            dto.setBrand("N/A");
                            dto.setBasePrice(BigDecimal.ZERO);
                            dto.setExpiryTime(new Date());
                            dto.setDeleted(false);

                            return dto;
                        })
                        .collect(Collectors.toList());
            }
        };

    }

}
