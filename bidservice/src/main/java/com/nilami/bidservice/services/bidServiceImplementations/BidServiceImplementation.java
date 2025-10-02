package com.nilami.bidservice.services.bidServiceImplementations;

import java.math.BigDecimal;
import java.util.Optional;

import com.nilami.bidservice.dto.BidDTO;
import com.nilami.bidservice.models.Bid;
import com.nilami.bidservice.services.BidService;

public class BidServiceImplementation implements BidService {

    @Override
    public Optional<Bid> getLastBid(String itemId) {
           return null;
    }

    @Override
    public BidDTO placeBid(String itemId, BigDecimal price, String userId, String idempotentKey) throws Exception {
        return null;
    }

    @Override
    public String setIdempotentKey() {
            return null;
    }

    @Override
    public Boolean checkIfIdempotentKeyExists(String key) {
         return null;
    }
    
}
