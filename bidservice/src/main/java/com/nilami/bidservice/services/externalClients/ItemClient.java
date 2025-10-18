package com.nilami.bidservice.services.externalClients;



import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import com.nilami.bidservice.configs.FeignHeaderForwardingConfig;


import com.nilami.bidservice.dto.ItemDTO;

@FeignClient(name = "CATALOG-SERVICE", url = "${CATALOG_SERVICE_HOST}", configuration = FeignHeaderForwardingConfig.class)
public interface ItemClient {

    @GetMapping("/api/v1/items/{id}")
    ItemDTO getItem(@PathVariable("id") String itemId);

    @GetMapping("/api/v1/items/{id}/expiry")
    Boolean checkExpiry(@PathVariable("id") String itemId);

}