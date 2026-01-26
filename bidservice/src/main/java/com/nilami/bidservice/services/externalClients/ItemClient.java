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


@FeignClient(
    name = "catalog-server-service",
    url="${CATALOG_SERVICE_HOST}",
    configuration = FeignHeaderForwardingConfig.class)
public interface ItemClient {

    @GetMapping("/api/v1/items/{id}")
    ItemDTO getItem(@PathVariable("id") String itemId);

    @GetMapping("/api/v1/items/{id}/expiry")
    Boolean checkExpiry(@PathVariable("id") String itemId);

    @PostMapping("/api/v1/items/details")
    List<SimplifiedItemDTO> getItemDetails(@RequestBody List<String> itemIds);

}