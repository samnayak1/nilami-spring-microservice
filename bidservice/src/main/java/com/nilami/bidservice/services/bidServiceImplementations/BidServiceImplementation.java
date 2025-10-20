package com.nilami.bidservice.services.bidServiceImplementations;

import java.math.BigDecimal;

import java.util.List;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nilami.bidservice.controllers.requestTypes.BalanceReservationRequest;
import com.nilami.bidservice.dto.ApiResponse;
import com.nilami.bidservice.dto.BalanceReservationResponse;
import com.nilami.bidservice.dto.BidDTO;
import com.nilami.bidservice.dto.ItemDTO;
import com.nilami.bidservice.dto.UserDTO;
import com.nilami.bidservice.exceptions.BidPlacementException;
import com.nilami.bidservice.exceptions.InvalidBidException;
import com.nilami.bidservice.exceptions.ItemExpiredException;
import com.nilami.bidservice.models.Bid;
import com.nilami.bidservice.repositories.BidRepository;
import com.nilami.bidservice.services.BidService;
import com.nilami.bidservice.services.externalClients.ItemClient;
import com.nilami.bidservice.services.externalClients.UserClient;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BidServiceImplementation implements BidService {

    private BidRepository bidRepository;

    private UserClient userClient;

    private ItemClient itemClient;

    @Override
    public Optional<BidDTO> getLastBid(String itemId) {

        Optional<Bid> lastBid = bidRepository.findTopByOrderByCreatedDesc();

        if (lastBid.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(convertToBidDTO(lastBid.get()));
    }

    @Override
    public BidDTO placeBid(String itemId, BigDecimal price, String userId) throws Exception {
       String reservationId = null;
        try {
             String idempotentKey = UUID.randomUUID().toString();
            // check for last bid and that bid should not exceed previous bid
              Optional<BidDTO> lastBid = this.getLastBid(itemId);

            if (lastBid.isPresent()) {
                BigDecimal lastBidPrice = lastBid.get().getPrice();
                if (price.compareTo(lastBidPrice) <= 0) {
                    throw new InvalidBidException("Bid price must be higher than the last bid: " + lastBidPrice);
                }
            }
       
            Boolean isExpired = itemClient.checkExpiry(itemId);
            
            if (isExpired == null || isExpired) {
                throw new ItemExpiredException("Item " + itemId + " has expired. Bidding is closed.");
            }

             BalanceReservationRequest reserveRequest = 
                new BalanceReservationRequest(userId, price, idempotentKey);

            ApiResponse<BalanceReservationResponse> reservationResponse = 
                userClient.reserveBalance(reserveRequest);
              if (!reservationResponse.getSuccess() || reservationResponse.getData() == null) {
                throw new BidPlacementException(
                    "Failed to reserve balance: " + reservationResponse.getMessage()
                );
            }
          reservationId = reservationResponse.getData().getReservationId();
            Bid placedBidEntity = Bid.builder()
                    .itemId(itemId)
                    .price(price)
                    .creatorId(userId)
        
                    .build();

            Bid placedBid = bidRepository.save(placedBidEntity);

              ApiResponse<Void> commitResponse = 
                userClient.commitBalanceReservation(reservationId);
            
            if (!commitResponse.getSuccess()) {
               
                throw new BidPlacementException(
                    "Failed to commit balance reservation: " + commitResponse.getMessage()
                );
            }
     return this.convertToBidDTO(placedBid);

            // catch statement, cancel the reservation

        } catch (Exception e) {
                 if (reservationId != null) {
                try {
                 
                    ApiResponse<Void> cancelResponse = 
                        userClient.cancelBalanceReservation(reservationId);
                    
                    if (cancelResponse.getSuccess()) {
                       System.out.println("Balance reservation cancelled successfully: {}"+reservationId);
                    } else {
                        System.out.println("Balance reservation cancelled successfully: {}"+ 
                                 reservationId + cancelResponse.getMessage());
                    }
                } catch (Exception rollbackException) {
                  
                     System.out.println("ROLLBACK EXPECTION"+rollbackException.getMessage());
                    // TODO: make a kafka queue to send to customer support for manual intervention
                }
            }
            
      
            throw new BidPlacementException("Failed to place bid: " + e.getMessage(), e);
        }
        }

    


    @Override
    public List<Bid> getBidsOfItems(String itemId) {
        List<Bid> bidsOfItem=bidRepository.findByItemIdOrderByCreatedAtDesc(UUID.fromString(itemId));
    
        return bidsOfItem;

    }

    public BidDTO convertToBidDTO(Bid bid) {

        String userId = bid.getCreatorId();

        String itemId = bid.getItemId();

        ApiResponse<UserDTO> userResponse = userClient.getUserDetails(userId);

        UserDTO user = (UserDTO) userResponse.getData();

        String userName = user.getName();

        ItemDTO item = itemClient.getItem(itemId);

        String itemName = item.getTitle();

        return BidDTO.builder().id(bid.getId().toString())
                .itemId(itemId)
                .itemName(itemName)
                .created(bid.getCreated())
                .price(bid.getPrice())
                .creatorName(userName)
                .build();

    }

}
