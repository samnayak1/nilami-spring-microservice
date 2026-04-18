package com.nilami.bidservice.services.bidServiceImplementations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nilami.bidservice.controllers.requestTypes.BalanceRequest;
import com.nilami.bidservice.controllers.requestTypes.BalanceReservationRequest;
import com.nilami.bidservice.dto.ApiResponse;
import com.nilami.bidservice.dto.BalanceReservationResponse;
import com.nilami.bidservice.dto.BidDTO;
import com.nilami.bidservice.dto.BidEventMessageQueuePayload;
import com.nilami.bidservice.dto.GetBidsOfUserAlongWithHighestBidForItemResponseBody;
import com.nilami.bidservice.dto.GetBidsOfUserWithItemDetails;
import com.nilami.bidservice.dto.GetHighestBidAlongWithItemIds;
import com.nilami.bidservice.dto.GetIdempotentKeyResponse;
import com.nilami.bidservice.dto.ItemDTO;

import com.nilami.bidservice.dto.SimplifiedItemDTO;
import com.nilami.bidservice.dto.UserDTO;
import com.nilami.bidservice.exceptions.BidLessThanItemException;

import com.nilami.bidservice.exceptions.IdempotentKeyException;
import com.nilami.bidservice.exceptions.InvalidBidException;
import com.nilami.bidservice.exceptions.ItemExpiredException;

import com.nilami.bidservice.exceptions.NoIdempotentKeyException;

import com.nilami.bidservice.models.Bid;
import com.nilami.bidservice.models.BidStatus;
import com.nilami.bidservice.models.IdempotentKeys;

import com.nilami.bidservice.models.SagaLogs;
import com.nilami.bidservice.models.SagaState;
import com.nilami.bidservice.repositories.BidRepository;
import com.nilami.bidservice.repositories.IdempotentKeyRepository;

import com.nilami.bidservice.repositories.SagaLogsRepository;
import com.nilami.bidservice.services.BidEventPublisher;
import com.nilami.bidservice.services.BidService;
import com.nilami.bidservice.services.externalClients.ItemClient;
import com.nilami.bidservice.services.externalClients.UserClient;

import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class BidServiceImplementation implements BidService {

    private final BidRepository bidRepository;

    private final IdempotentKeyRepository idempotentKeyRepository;

    private final SagaLogsRepository sagaLogsRepository;

   // private final OutboxRepository outboxRepository;

    private final UserClient userClient;

    private final ItemClient itemClient;

    private final BidEventPublisher bidEventPublisher;

 //   private final ObjectMapper objectMapper;

    

    @Override
    public Optional<BidDTO> getLastBid(String itemId) {
        return bidRepository.findTopByItemIdOrderByCreatedDesc(UUID.fromString(itemId))
                .map(this::convertToBidDTO);
    }

    public Optional<Bid> getLatestBidOfItemAndUserIfExists(String itemId, String userId) {
        return bidRepository.findTopByItemIdAndCreatorIdOrderByCreatedDesc(UUID.fromString(itemId),
                UUID.fromString(userId));
    }

    @Override
    public BidDTO placeBid(String itemId, BigDecimal price, String userId, String idempotentKey) {
        String reservationId = null;
        UUID sagaId = UUID.randomUUID();
        BigDecimal priceUserHasToBid = BigDecimal.valueOf(0.0);
        try {

            // check for last bid and that bid should not exceed previous bid
            // check all contraints that the bid has to make like it's past the expiry date

            log.debug("before getting last bid of item {} with price {} and userId", itemId, price, userId);

            //PESSIMISTIC LOCK IS USED IN THE REPOSITORY LAYER FOR THIS FIND BY ID CALL.
            //  SO WHEN TWO REQUESTS COME WITH SAME IDEMPOTENT KEY, 
            // ONE OF THEM WILL WAIT TILL THE OTHER COMPLETES THE TRANSACTION AND RELEASES THE LOCK. 
            Optional<BidDTO> lastBid = this.getLastBid(itemId);

            ItemDTO item = itemClient.getItem(itemId);

            log.debug("Item id {} fetched with item name", itemId, item.getTitle());

            validateBidRequest(itemId, price, item, lastBid);

            // If a user has already bid for an item and they bid again, then the amount to
            // subtract from their balance is the
            // difference in the amount they already bid and the current bid.

            Optional<Bid> latestBidOfUser = this.getLatestBidOfItemAndUserIfExists(itemId, userId);

            BigDecimal balanceReservationSubtrahend = BigDecimal.valueOf(0.0);
            if (!latestBidOfUser.isEmpty()) {
                balanceReservationSubtrahend = price.subtract(latestBidOfUser.get().getPrice());
            } else {
                balanceReservationSubtrahend = price;
            }

            priceUserHasToBid = balanceReservationSubtrahend;
            log.debug("price user has to bid {}", priceUserHasToBid);

            // make the idempotent key checks

            IdempotentKeys entity = idempotentKeyRepository
                    .findById(UUID.fromString(idempotentKey))
                    .orElseThrow(() -> new NoIdempotentKeyException("The idempotent key does not exist."));

            if (entity.getBidStatus() != BidStatus.PENDING) {
                throw new IdempotentKeyException("The bid related to this idempotent key is already in use.");
            }

            // Actual bid process happens from here onwards


            // bid status logs is set from pending to creating i.e the rollback starts now if fails

            entity.setBidStatus(BidStatus.CREATING);
            idempotentKeyRepository.save(entity);

            // add to SAGA with the status that we are creating a new Bid

            SagaLogs saga = SagaLogs.builder()
                    .sagaId(sagaId)
                    .itemId(UUID.fromString(itemId))
                    .userId(UUID.fromString(userId))
                    .currentState(SagaState.STARTED)
                    .sagaType("PLACE_BID")
                    .bidAmount(price)
                    .build();

            sagaLogsRepository.save(saga);

            // We first reserve a certain amount (bid amount - previous user's bid if
            // exists) in our user's database
            BalanceReservationRequest reserveRequest = new BalanceReservationRequest(userId,
                    priceUserHasToBid, idempotentKey);

            ApiResponse<BalanceReservationResponse> reservationResponse = userClient.reserveBalance(reserveRequest);

            log.debug("The balance reservation for itemId {} and for user {} for price {} is evaluated to {}", itemId,
                    userId, reserveRequest.getAmount(), reservationResponse.getSuccess());

            sagaLogsRepository.updateStatus(sagaId, SagaState.FUNDS_RESERVED);
            reservationId = reservationResponse.getData().getReservationId();
            Bid placedBidEntity = Bid.builder()
                    .itemId(UUID.fromString(itemId))
                    .price(price)
                    .creatorId(UUID.fromString(userId))
                    .sagaId(sagaId)
                    .build();

            Bid placedBid = bidRepository.save(placedBidEntity);
            log.debug("placed bid is successful for {} with price: {}", placedBid.getItemId(), placedBid.getPrice());
            sagaLogsRepository.updateStatus(sagaId, SagaState.BID_PLACED);
            log.debug("bid placed for item: " + placedBid.getItemId());
            ApiResponse<Void> commitResponse = userClient.commitBalanceReservation(reservationId);
            log.debug("bid commited for item: " + placedBid.getItemId() + "response evaluated to "
                    + commitResponse.getSuccess());
            sagaLogsRepository.updateStatus(sagaId, SagaState.FUNDS_COMMITED);

            BidEventMessageQueuePayload messageQueuePayload = new BidEventMessageQueuePayload(
                    UUID.randomUUID(), //eventId which is unique for idempotent requests
                    UUID.fromString(itemId),
                    placedBid.getId(),
                    price,
                    UUID.fromString(userId),
                    Instant.now()
                );

            // String payload = objectMapper.writeValueAsString(messageQueuePayload);

            // OutboxEvent outboxEvent = OutboxEvent.builder()
            //         .eventType(OutboxEventType.BidPlaced)
            //         .payload(payload)
            //         .aggregateId(UUID.fromString(itemId))
            //         .aggregateType("BID")
            //         .status(OutboxStatus.NEW)
            //         .build();
            // outboxRepository.save(outboxEvent);



            // fire and forget. This sends an event to the websocket service for the service to relay the latest bid for every other user
            try {
                bidEventPublisher.sendBidEventToQueue(messageQueuePayload);
            } catch (Exception ex) {
                log.warn("Failed to send bid event to queue. Ignoring.", ex);
            }

            entity.setBidStatus(BidStatus.COMPLETED);
            idempotentKeyRepository.save(entity);

            sagaLogsRepository.updateStatus(sagaId, SagaState.COMPLETED);

            return this.convertToBidDTO(placedBid);
        }
        // } catch (JsonProcessingException e) {
        //     throw new IllegalStateException("Failed to serialize outbox payload", e);
        // }
        
        catch (PessimisticLockException e) {

            log.warn("Another request for idempotent key {}", idempotentKey);
            throw new IdempotentKeyException("This bid is already being processed by another request.");

        } catch (Exception e) {
            log.debug("Exception {}", e.getCause());
            if (!(e instanceof NoIdempotentKeyException || e instanceof IdempotentKeyException)) {
                try {
                    Optional<IdempotentKeys> keyResponseFromDatabase = idempotentKeyRepository
                            .findById(UUID.fromString(idempotentKey));

                    if (keyResponseFromDatabase.isPresent()) {
                        IdempotentKeys entity = keyResponseFromDatabase.get();
                        entity.setBidStatus(BidStatus.REJECTED);
                        idempotentKeyRepository.save(entity);
                    }
                } catch (PessimisticLockException ole) {

                    log.debug("key updated by another thread");
                }

            }

            Optional<SagaLogs> sagaOptional = sagaLogsRepository.findById(sagaId);
            log.debug("saga fetched");

            List<SagaState> eventsToRevert = new ArrayList<SagaState>();
            
            if (sagaOptional.isPresent()) {
                SagaLogs saga = sagaOptional.get();
                switch (saga.getCurrentState()) {

                    // create an array, add the cases when loop, and then finally create a function
                    // that just loops through
                    case STARTED:
                        log.debug("SAGA ERROR CAUGHT WHEN STARTED");
                        // nothing to do because we just started
                        break;
                    case BID_PLACED:
                        // unplace the bid
                        log.debug("SAGA ERROR CAUGHT WHEN BID PLACED");
                        eventsToRevert.add(SagaState.BID_PLACED);

                        // unreserve the funds
                        eventsToRevert.add(SagaState.FUNDS_RESERVED);
                        break;

                    case FUNDS_COMMITED:
                        log.debug("SAGA ERROR CAUGHT WHEN FUNDS COMMITED");
                        // uncommit the funds
                        eventsToRevert.add(SagaState.FUNDS_COMMITED);
                        // unplace the bid
                        eventsToRevert.add(SagaState.BID_PLACED);
                        // unreserve the funds
                        eventsToRevert.add(SagaState.FUNDS_RESERVED);

                        break;
                    case FUNDS_RESERVED:
                        log.debug("SAGA ERROR CAUGHT WHEN FUNDS RESERVED");

                        // unreserve the fund
                        eventsToRevert.add(SagaState.FUNDS_RESERVED);
                        break;
                    default:
                        log.error(
                                "Unexpected case in the bid service catch block switch statement. Please look into this. ");
                        break;

                }

                String finalReservationId = reservationId;
                BigDecimal finalPriceUserHasToBid = priceUserHasToBid;

                eventsToRevert.forEach(event -> {
                    switch (event) {
                        case BID_PLACED:
                            log.debug("deleting the bid placed by saga id: {}", sagaId);
                            this.deleteBid(sagaId.toString());
                            break;
                        case FUNDS_COMMITED:
                            log.debug("Compensating the user: {}", userId);
                            BalanceRequest balanceRequest = new BalanceRequest(userId, finalPriceUserHasToBid);
                            userClient.addBalanceToUser(balanceRequest);
                            break;
                        case FUNDS_RESERVED:
                            log.debug("Cancelling the reservation: {}", finalReservationId);
                            userClient.cancelBalanceReservation(finalReservationId);

                            break;

                        default:
                            log.error(
                                    "Unexpected case in the bid service catch block switch statement. Please look into this. ");
                            break;

                    }

                });

                sagaLogsRepository.updateStatus(sagaId, SagaState.REJECTED);
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

    @Override
    public GetIdempotentKeyResponse getIdempotentKey(String itemId, BigDecimal bidAmount, String userId) {

        IdempotentKeys idempotentKey = IdempotentKeys.builder()
                .bidAmount(bidAmount)
                .bidStatus(BidStatus.PENDING)
                .creatorId(UUID.fromString(userId))
                .itemId(UUID.fromString(itemId))
                .build();

        idempotentKeyRepository.save(idempotentKey);

        GetIdempotentKeyResponse idempotentKeyResponse = new GetIdempotentKeyResponse(
                idempotentKey.getId().toString(),
                UUID.fromString(itemId),
                bidAmount,
                Instant.now());

        return idempotentKeyResponse;

    }

    @Override
    public Long deleteBid(String sagaId) {

        Long numberOfRecordsDeleted = bidRepository.deleteBySagaId(UUID.fromString(sagaId));

        return numberOfRecordsDeleted;
    }

    @Override
    public Map<String, BigDecimal> getItemsHighestBidGivenItemIds(List<UUID> itemIds) {

        List<GetHighestBidAlongWithItemIds> itemIdsWithHighestBids = bidRepository
                .getItemsHighestBidGivenItemIds(itemIds);
        System.out.print("itemIdsWithHighestBids:" + itemIdsWithHighestBids);
        HashMap<String, BigDecimal> itemIdToHighestBidMap = new HashMap<>();

        itemIdsWithHighestBids.forEach(itemToHighestBidKeyValue -> {
            itemIdToHighestBidMap.put(
                    itemToHighestBidKeyValue.getItemId().toString(),
                    itemToHighestBidKeyValue.getHighestBidPrice());
        });
        System.out.print("itemIdToHighestBid:" + itemIdToHighestBidMap);
        return itemIdToHighestBidMap;

    }

    private void validateBidRequest(String itemId, BigDecimal price, ItemDTO item,
            Optional<BidDTO> lastBid) {
        if (lastBid.isPresent() && price.compareTo(lastBid.get().getPrice()) <= 0) {
            throw new InvalidBidException("Bid price must be higher than the last bid: "
                    + lastBid.get().getPrice());
        }

        if (price.compareTo(item.getBasePrice()) < 0) {
            throw new BidLessThanItemException("Price must be higher than the item's base price");
        }

        if (item.getExpiryTime().before(Date.from(Instant.now()))) {
            throw new ItemExpiredException("Item " + itemId + " has expired. Bidding is closed.");
        }
    }

}
