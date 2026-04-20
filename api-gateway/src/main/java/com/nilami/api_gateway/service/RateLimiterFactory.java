package com.nilami.api_gateway.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimiterFactory {

    private final SlidingWindowRateLimiter slidingWindowRateLimiter;
    // If we have token bucket in the future, we can add it here and return based on type

    public RateLimiter get(String type) {
        return switch (type) {
            case "sliding" -> slidingWindowRateLimiter;
            default -> throw new IllegalArgumentException("Unknown type");
        };
    }
}
