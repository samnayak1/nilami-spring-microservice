package com.nilami.bidservice.services;

import java.math.BigDecimal;
import java.util.Optional;

import com.nilami.bidservice.dto.BidDTO;
import com.nilami.bidservice.models.Bid;

public interface BidService {
      public Optional<Bid> getLastBid(String itemId);
      public BidDTO placeBid(String itemId, BigDecimal price, String userId,String idempotentKey) throws Exception;
      public String setIdempotentKey();
      public Boolean checkIfIdempotentKeyExists(String key);
}
