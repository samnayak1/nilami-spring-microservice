package com.nilami.catalogservice.services.externalClients;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.nilami.catalogservice.dto.ApiResponse;
import com.nilami.catalogservice.dto.GetHighestBidsRequest;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BidClientFallbackFactory implements FallbackFactory<BidClient> {
    @Override
    public BidClient create(Throwable cause) {
        log.error("bid client failed {}", cause.getMessage());
        Map<String, BigDecimal> emptyData = Map.of("", BigDecimal.ZERO);
        return new BidClient() {
            @Override
            public ApiResponse<Map<String, BigDecimal>> getHighestBidsForItems(GetHighestBidsRequest request) {
                return new ApiResponse<>(false, "Fallback happened because-" + cause.getMessage(), emptyData);
            }
        };
    }
}