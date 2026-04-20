package com.nilami.bidservice.services.externalClients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nilami.bidservice.configs.FeignHeaderForwardingConfig;

import com.nilami.bidservice.dto.ItemDTO;
import com.nilami.bidservice.dto.SimplifiedItemDTO;

@FeignClient(name = "catalog-server-service", url = "${CATALOG_SERVICE_HOST}", configuration = FeignHeaderForwardingConfig.class, fallbackFactory = ItemClientFallbackFactory.class)
public interface ItemClient {

    @GetMapping("/api/items/v1/{id}")
    ItemDTO getItem(@PathVariable("id") String itemId);

    @PostMapping("/api/items/v1/details")
    List<SimplifiedItemDTO> getItemDetails(@RequestBody List<String> itemIds);

}