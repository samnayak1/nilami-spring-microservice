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
import com.nilami.bidservice.exceptions.BidLessThanItemException;
import com.nilami.bidservice.exceptions.BidPlacementException;
import com.nilami.bidservice.exceptions.InvalidBidException;
import com.nilami.bidservice.exceptions.ItemExpiredException;
import com.nilami.bidservice.models.Bid;
import com.nilami.bidservice.repositories.BidRepository;
import com.nilami.bidservice.services.BidService;
import com.nilami.bidservice.services.externalClients.ItemClient;
import com.nilami.bidservice.services.externalClients.UserClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class BidServiceImplementation implements BidService {

    private final BidRepository bidRepository;

    private final UserClient userClient;

    private final ItemClient itemClient;

    @Override
    public Optional<BidDTO> getLastBid(String itemId) {
        return bidRepository.findTopByItemIdOrderByCreatedDesc(UUID.fromString(itemId))
                .map(this::convertToBidDTO);
    }

    @Override
    public BidDTO placeBid(String itemId, BigDecimal price, String userId) throws Exception {
        String reservationId = null;
        try {
            String idempotentKey = UUID.randomUUID().toString();
            // check for last bid and that bid should not exceed previous bid
            log.info("before getting last bid of item {} with price {} and userId", itemId, price, userId);
            Optional<BidDTO> lastBid = this.getLastBid(itemId);

            if (lastBid.isPresent()) {
                BigDecimal lastBidPrice = lastBid.get().getPrice();
                log.info("price of the last bid {} for item {}", lastBidPrice, lastBid.get().getItemId());
                if (price.compareTo(lastBidPrice) <= 0) {
                    throw new InvalidBidException("Bid price must be higher than the last bid: " + lastBidPrice);
                }
            }

            ItemDTO item = itemClient.getItem(itemId);
            if (price.compareTo(item.getBasePrice()) < 0) {
                throw new BidLessThanItemException("Price must be higher than the item's base price");
            }
            log.info("Item id {} fetched with item name", itemId, item.getTitle());

            Boolean isExpired = itemClient.checkExpiry(itemId);
            log.info("The expiry of the item {} is evaluated to {}", itemId, isExpired);
            if (isExpired == null || isExpired) {
                throw new ItemExpiredException("Item " + itemId + " has expired. Bidding is closed.");
            }
            // We first reserve a certain amount in our user's database
            BalanceReservationRequest reserveRequest = new BalanceReservationRequest(userId, price, idempotentKey);

            ApiResponse<BalanceReservationResponse> reservationResponse = userClient.reserveBalance(reserveRequest);
            log.info("The balance reservation for itemId {} and for user {} for price {} is evaluated to {}", itemId,
                    userId, reserveRequest.getAmount(), reservationResponse.getSuccess());
            if (!reservationResponse.getSuccess() || reservationResponse.getData() == null) {
                throw new BidPlacementException(
                        "Failed to reserve balance: " + reservationResponse.getMessage());
            }
            reservationId = reservationResponse.getData().getReservationId();
            Bid placedBidEntity = Bid.builder()
                    .itemId(UUID.fromString(itemId))
                    .price(price)
                    .creatorId(UUID.fromString(userId))
                    .build();

            Bid placedBid = bidRepository.save(placedBidEntity);
            log.info("bid placed for item: " + placedBid.getItemId());
            ApiResponse<Void> commitResponse = userClient.commitBalanceReservation(reservationId);
            log.info("bid commited for item: " + placedBid.getItemId() + "response evaluated to "
                    + commitResponse.getSuccess());
            if (!commitResponse.getSuccess()) {

                throw new BidPlacementException(
                        "Failed to commit balance reservation: " + commitResponse.getMessage());
            }
            return this.convertToBidDTO(placedBid);

            // catch statement, cancel the reservation

        } catch (Exception e) {
            if (reservationId != null) {
                try {

                    ApiResponse<Void> cancelResponse = userClient.cancelBalanceReservation(reservationId);

                    if (cancelResponse.getSuccess()) {
                        System.out.println("Balance reservation cancelled successfully: {}" + reservationId);
                    } else {
                        System.out.println("Balance reservation cancelled successfully: {}" +
                                reservationId + cancelResponse.getMessage());
                    }
                } catch (Exception rollbackException) {

                    log.error("ROLLBACK EXPECTION" + rollbackException.getMessage());
                    // Maybe a service to take manual intervention when this happens
                }
            }

            throw new BidPlacementException("Failed to place bid: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Bid> getBidsOfItems(String itemId) {
        List<Bid> bidsOfItem = bidRepository.findByItemIdOrderByCreatedDesc(UUID.fromString(itemId));

        return bidsOfItem;

    }

    public BidDTO convertToBidDTO(Bid bid) {

        UUID userId = bid.getCreatorId();

        UUID itemId = bid.getItemId();

        ApiResponse<UserDTO> userResponse = userClient.getUserDetails(userId.toString());

        UserDTO user = (UserDTO) userResponse.getData();

        String userName = user.getName();

        ItemDTO item = itemClient.getItem(itemId.toString());

        String itemName = item.getTitle();

        return BidDTO.builder().id(bid.getId())
                .itemId(itemId)
                .itemName(itemName)
                .created(bid.getCreated())
                .price(bid.getPrice())
                .creatorId(bid.getCreatorId())
                .creatorName(userName)
                .build();

    }

}
