package com.nilami.api_gateway.service;

public interface RateLimiter {
    public boolean isAllowed(String clientId);
}
