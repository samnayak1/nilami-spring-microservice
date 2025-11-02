package com.nilami.bidservice.services.externalClients.fallback;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nilami.bidservice.dto.ItemDTO;
import com.nilami.bidservice.services.externalClients.ItemClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ItemClientFallback implements ItemClient {


   

    @Override
    public ItemDTO getItem(String itemId) {

    log.warn("Fallback triggered for getItem with id: {}", itemId);
        
        ItemDTO fallbackItem = new ItemDTO();
        fallbackItem.setId(UUID.fromString(itemId));
        fallbackItem.setTitle("Item Unavailable");
        fallbackItem.setDescription("Service temporarily unavailable");
        fallbackItem.setBasePrice(BigDecimal.ZERO);
        fallbackItem.setBrand("N/A");
        fallbackItem.setPictureIds(Collections.emptyList());
        fallbackItem.setDeleted(false);
        fallbackItem.setCreatedAt(Instant.now());
        fallbackItem.setUpdatedAt(Instant.now());
        
        return fallbackItem;
    }

    @Override
    public Boolean checkExpiry(String itemId) {
        return false;
    }
}
