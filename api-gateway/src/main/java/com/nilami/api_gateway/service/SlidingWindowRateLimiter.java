package com.nilami.api_gateway.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.nilami.api_gateway.configs.RateLimitProperties;


@Service
public class SlidingWindowRateLimiter implements RateLimiter  {

    private final RedisTemplate<String, String> redisTemplate;


    private final RateLimitProperties rateLimitProperties;



    public SlidingWindowRateLimiter(RedisTemplate<String, String> redisTemplate,RateLimitProperties rateLimitProperties) {
        this.redisTemplate = redisTemplate;
        this.rateLimitProperties = rateLimitProperties;
    }

    public boolean isAllowed(String clientId) {
        return isAllowed(clientId, rateLimitProperties.getMaxRequests(), rateLimitProperties.getWindowSizeSeconds());
    }

    private boolean isAllowed(String clientId, int maxRequests, int windowSeconds) {
          //our key will be rate_limit:clientId:minute_that_we_are_in


        long now = Instant.now().getEpochSecond();

        //get the minute that we are in by modding the seconds
        long currentWindow  = now - (now % windowSeconds);
        long previousWindow = currentWindow - windowSeconds;
     
        String currentKey  = "rate_limit:" + clientId + ":" + currentWindow;
        String previousKey = "rate_limit:" + clientId + ":" + previousWindow;

       
        List<String> values = redisTemplate.opsForValue().multiGet(List.of(currentKey, previousKey));

        long currentCount  = values == null || values.get(0) == null ? 0L : Long.parseLong(values.get(0));
        long previousCount = values == null || values.get(1) == null ? 0L : Long.parseLong(values.get(1));

        //percent of minute we are in so let's say the time is 5:45 then we are 75% in the minute.
        double elapsedFraction = (double)(now % windowSeconds) / windowSeconds;


        double weightedCount   = previousCount * (1 - elapsedFraction) + currentCount;

        if (weightedCount >= maxRequests) {
            return false;
        }

        // Increment current window — TTL is 2x so the previous window key is still readable next window
        redisTemplate.opsForValue().increment(currentKey);
        redisTemplate.expire(currentKey, Duration.ofSeconds(windowSeconds * 2L));

        return true;
    }


}