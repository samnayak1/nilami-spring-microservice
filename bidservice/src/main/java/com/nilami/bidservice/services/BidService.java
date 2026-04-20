package com.nilami.bidservice.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.nilami.bidservice.dto.BidDTO;
import com.nilami.bidservice.dto.GetBidsOfUserWithItemDetails;
import com.nilami.bidservice.dto.GetHighestBidAlongWithItemIds;
import com.nilami.bidservice.dto.GetIdempotentKeyResponse;
import com.nilami.bidservice.models.Bid;


public interface BidService {
      public Optional<BidDTO> getLastBid(String itemId);
      public BidDTO placeBid(String itemId, BigDecimal price, String userId, String idempotentKey);
      public Long deleteBid(String sagaId);
      public List<Bid> getBidsOfItems(String itemId);
      public List<GetBidsOfUserWithItemDetails>  getBidsOfUserAlongWithHighestBidForItem(String userId);
      public GetIdempotentKeyResponse getIdempotentKey(String itemId,BigDecimal bidAmount,String userId);
      Map<String, BigDecimal> getItemsHighestBidGivenItemIds(List<UUID> itemIds);
      Map<String, GetHighestBidAlongWithItemIds> getHighestBids(List<UUID> itemIds);
}
