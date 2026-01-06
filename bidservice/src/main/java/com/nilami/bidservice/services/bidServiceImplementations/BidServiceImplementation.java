package com.nilami.bidservice.services.bidServiceImplementations;

import java.math.BigDecimal;
import java.time.Instant;

import java.util.HashMap;
import java.util.List;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nilami.bidservice.controllers.requestTypes.BalanceReservationRequest;
import com.nilami.bidservice.dto.ApiResponse;
import com.nilami.bidservice.dto.BalanceReservationResponse;
import com.nilami.bidservice.dto.BidDTO;
import com.nilami.bidservice.dto.BidEventMessageQueuePayload;
import com.nilami.bidservice.dto.GetBidsOfUserAlongWithHighestBidForItemResponseBody;
import com.nilami.bidservice.dto.GetBidsOfUserWithItemDetails;
import com.nilami.bidservice.dto.ItemDTO;
import com.nilami.bidservice.dto.SimplifiedItemDTO;
import com.nilami.bidservice.dto.UserDTO;
import com.nilami.bidservice.exceptions.BidLessThanItemException;
import com.nilami.bidservice.exceptions.BidPlacementException;
import com.nilami.bidservice.exceptions.InvalidBidException;
import com.nilami.bidservice.exceptions.ItemExpiredException;
import com.nilami.bidservice.models.Bid;
import com.nilami.bidservice.repositories.BidRepository;
import com.nilami.bidservice.services.BidEventPublisher;
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

    private final BidEventPublisher bidEventPublisher;

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
            log.debug("before getting last bid of item {} with price {} and userId", itemId, price, userId);
            Optional<BidDTO> lastBid = this.getLastBid(itemId);

            if (lastBid.isPresent()) {
                BigDecimal lastBidPrice = lastBid.get().getPrice();
                log.debug("price of the last bid {} for item {}", lastBidPrice, lastBid.get().getItemId());
                if (price.compareTo(lastBidPrice) <= 0) {
                    throw new InvalidBidException("Bid price must be higher than the last bid: " + lastBidPrice);
                }
            }

            ItemDTO item = itemClient.getItem(itemId);
            if (price.compareTo(item.getBasePrice()) < 0) {
                throw new BidLessThanItemException("Price must be higher than the item's base price");
            }
            log.debug("Item id {} fetched with item name", itemId, item.getTitle());

            Boolean isExpired = itemClient.checkExpiry(itemId);
            log.debug("The expiry of the item {} is evaluated to {}", itemId, isExpired);
            if (isExpired == null || isExpired) {
                throw new ItemExpiredException("Item " + itemId + " has expired. Bidding is closed.");
            }
            // We first reserve a certain amount in our user's database
            BalanceReservationRequest reserveRequest = new BalanceReservationRequest(userId, price, idempotentKey);

            ApiResponse<BalanceReservationResponse> reservationResponse = userClient.reserveBalance(reserveRequest);
            log.debug("The balance reservation for itemId {} and for user {} for price {} is evaluated to {}", itemId,
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
            log.debug("bid placed for item: " + placedBid.getItemId());
            ApiResponse<Void> commitResponse = userClient.commitBalanceReservation(reservationId);
            log.debug("bid commited for item: " + placedBid.getItemId() + "response evaluated to "
                    + commitResponse.getSuccess());
            if (!commitResponse.getSuccess()) {

                throw new BidPlacementException(
                        "Failed to commit balance reservation: " + commitResponse.getMessage());
            }
            bidEventPublisher.sendBidEventToQueue(new BidEventMessageQueuePayload(
                    UUID.fromString(itemId),
                    placedBid.getId(),
                    price,
                    UUID.fromString(userId),
                    Instant.now().toString()));
            return this.convertToBidDTO(placedBid);

           

        } catch (Exception e) {

        if (reservationId != null) {
            try {
                userClient.cancelBalanceReservation(reservationId);
            } catch (Exception rollbackException) {
                log.error("Rollback failed", rollbackException);
            }
        }

        throw e; 
    }
}

    @Override
    public List<Bid> getBidsOfItems(String itemId) {
        List<Bid> bidsOfItem = bidRepository
                .findByItemIdOrderByCreatedDesc(UUID.fromString(itemId));

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

    @Override
    public List<GetBidsOfUserWithItemDetails> getBidsOfUserAlongWithHighestBidForItem(
            String userId) {

        List<GetBidsOfUserAlongWithHighestBidForItemResponseBody> bidsOfUser = bidRepository
                .getBidsOfUserAlongWithHighestBidForItemRepositoryQuery(UUID.fromString(userId));
        log.debug("Bids returned from bid repository: " + bidsOfUser);
        List<String> itemIds = bidsOfUser.stream().map(bid -> {
            return bid.getItemId().toString();
        }).distinct().collect(Collectors.toList());
        log.debug("items: " + itemIds);
        List<SimplifiedItemDTO> items = itemClient.getItemDetails(itemIds);
        log.debug("items returned from itemClient: " + items);
        HashMap<String, SimplifiedItemDTO> itemIdToDetailsMap = new HashMap<>();

        for (SimplifiedItemDTO item : items) {
            itemIdToDetailsMap.put(item.getId().toString(), item);
        }

        List<GetBidsOfUserWithItemDetails> bidsWithItemDetails = bidsOfUser.stream()
                .map(bid -> new GetBidsOfUserWithItemDetails(
                        bid.getId(),
                        bid.getCreatorId(),
                        bid.getItemId(),
                        bid.getPrice(),
                        bid.getCreatedAt(),
                        bid.getIsHighestBid(),
                        itemIdToDetailsMap.get(
                                bid.getItemId().toString()).getTitle(),
                        itemIdToDetailsMap.get(
                                bid.getItemId().toString()).getBasePrice(),
                        itemIdToDetailsMap.get(
                                bid.getItemId().toString()).getBrand(),
                        itemIdToDetailsMap.get(
                                bid.getItemId().toString()).getExpiryTime(),
                        itemIdToDetailsMap.get(
                                bid.getItemId().toString()).isDeleted()

                )).collect(Collectors.toList());

        return bidsWithItemDetails;

    }

}
