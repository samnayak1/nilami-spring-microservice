package com.nilami.bidservice.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;



import com.nilami.bidservice.dto.BidDTO;
import com.nilami.bidservice.models.Bid;


public interface BidService {
      public Optional<BidDTO> getLastBid(String itemId);
      public BidDTO placeBid(String itemId, BigDecimal price, String userId) throws Exception;
      public List<Bid> getBidsOfItems(String itemId);
}
