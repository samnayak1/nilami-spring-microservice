package com.nilami.bidservice.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.nilami.bidservice.dto.BidDTO;


public interface BidService {
      public Optional<BidDTO> getLastBid(String itemId);
      public BidDTO placeBid(String itemId, BigDecimal price, String userId,String idempotentKey) throws Exception;
      public String setIdempotentKey();
      public Boolean checkIfIdempotentKeyExists(String key);
      public List<BidDTO> getBidsOfItems(String itemId);
}
