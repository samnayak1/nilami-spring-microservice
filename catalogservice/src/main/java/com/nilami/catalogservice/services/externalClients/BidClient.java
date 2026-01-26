package com.nilami.catalogservice.services.externalClients;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nilami.catalogservice.configs.FeignHeaderForwardingConfig;
import com.nilami.catalogservice.dto.ApiResponse;
import com.nilami.catalogservice.dto.GetHighestBidsRequest;

@FeignClient(name = "bid-server-service",
url="${BID_SERVICE_HOST}",
configuration = FeignHeaderForwardingConfig.class)
public interface BidClient {
        @PostMapping("/api/v1/bids/highest-bids")
    ApiResponse<Map<String, BigDecimal>> getHighestBidsForItems(@RequestBody GetHighestBidsRequest request);

}
