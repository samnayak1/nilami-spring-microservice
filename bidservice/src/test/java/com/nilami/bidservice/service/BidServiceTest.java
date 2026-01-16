package com.nilami.bidservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nilami.bidservice.controllers.requestTypes.BalanceReservationRequest;
import com.nilami.bidservice.dto.ApiResponse;
import com.nilami.bidservice.dto.BalanceReservationResponse;
import com.nilami.bidservice.dto.BidDTO;
import com.nilami.bidservice.dto.BidEventMessageQueuePayload;
import com.nilami.bidservice.dto.ItemDTO;
import com.nilami.bidservice.dto.Roles;
import com.nilami.bidservice.dto.UserDTO;
import com.nilami.bidservice.exceptions.BidLessThanItemException;

import com.nilami.bidservice.exceptions.InvalidBidException;
import com.nilami.bidservice.exceptions.ItemExpiredException;

import com.nilami.bidservice.models.Bid;
import com.nilami.bidservice.models.BidStatus;
import com.nilami.bidservice.models.IdempotentKeys;
import com.nilami.bidservice.models.SagaLogs;
import com.nilami.bidservice.models.SagaState;
import com.nilami.bidservice.repositories.BidRepository;
import com.nilami.bidservice.repositories.IdempotentKeyRepository;
import com.nilami.bidservice.repositories.SagaLogsRepository;
import com.nilami.bidservice.services.BidEventPublisher;
import com.nilami.bidservice.services.bidServiceImplementations.BidServiceImplementation;
import com.nilami.bidservice.services.externalClients.ItemClient;
import com.nilami.bidservice.services.externalClients.UserClient;

import feign.FeignException;
import feign.Request;

@ExtendWith(MockitoExtension.class)

class BidServiceTest {

        @Mock
        private BidRepository bidRepository;

        @Mock
        private IdempotentKeyRepository idempotentKeyRepository;

        @Mock
        private SagaLogsRepository sagaLogsRepository;

        @Mock
        private UserClient userClient;

        @Mock
        private ItemClient itemClient;

        @Mock
        private BidEventPublisher bidEventPublisher;

        @InjectMocks
        private BidServiceImplementation bidService;

        private static final String USER_EMAIL = "example@email.com";
        private static final String USER_PROFILE_PICTURE = "https://www.example.fgk/pic1";
        private static final String USER_NAME = "Ange Postecoglu";
        private static final String ITEM_BRAND = "Nike";
        private static final String ITEM_NAME = "Antique Shoes";
        private static final String ITEM_DESCRIPTION = "Shoes worn in the 18th Century";
        private static final List<String> PICTURE_IDS = List.of("pic1", "pic2", "pic3");
        private static final String IDEMPOTENT_KEY = "idempotentKey456";

        private UUID itemId;
        private UUID userId;
        private UUID sellerId;
        private UUID categoryId;
        private UUID idempotentKey;
        private UUID previousBidUserId;

        @BeforeEach
        void setUp() {
                itemId = UUID.randomUUID();
                userId = UUID.randomUUID();
                sellerId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
                idempotentKey = UUID.randomUUID();
                previousBidUserId = UUID.randomUUID();
        }

        @Test

        void testPlaceBid_Success() {

                BigDecimal bidAmount = BigDecimal.valueOf(12);
                BigDecimal userBalance = BigDecimal.valueOf(30);
                BigDecimal lastBidAmount = BigDecimal.valueOf(11);
                BigDecimal itemBasePrice = BigDecimal.valueOf(10);
                BigDecimal usersPreviousBidForItemPrice = BigDecimal.valueOf(10);
                // BigDecimal amountUserHasToBid=
                // bidAmount.subtract(usersPreviousBidForItemPrice);

                SagaLogs sagaLog = SagaLogs.builder()
                                .sagaId(UUID.randomUUID())
                                .itemId(itemId)
                                .userId(userId)
                                .currentState(SagaState.STARTED)
                                .bidAmount(bidAmount)
                                .build();

                IdempotentKeys idempotentKeyEntity = IdempotentKeys.builder()
                                .id(idempotentKey)
                                .bidAmount(bidAmount)
                                .bidStatus(BidStatus.PENDING)
                                .creatorId(userId)
                                .itemId(itemId)
                                .build();

                UserDTO user = createUserDTO(userId,userBalance);
                UserDTO previoudUserWhoBid=createUserDTO(previousBidUserId, userBalance);
                ItemDTO item = createItemDTO(itemBasePrice, Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
                Bid lastBid = createBid(lastBidAmount, previousBidUserId);
                Bid expectedBid = createBid(bidAmount, userId);
                Bid usersPreviousBid = createBid(usersPreviousBidForItemPrice, userId);

                BalanceReservationResponse balanceResponse = createBalanceReservationResponse();

                when(userClient.getUserDetails(userId.toString()))
                                .thenReturn(new ApiResponse<>(true, "user created", user));
                when(userClient.getUserDetails(previousBidUserId.toString()))
                                .thenReturn(new ApiResponse<>(true, "user created", previoudUserWhoBid));
                when(itemClient.getItem(itemId.toString())).thenReturn(item);
                when(bidRepository.findTopByItemIdOrderByCreatedDesc(itemId))
                                .thenReturn(Optional.of(lastBid));
                when(userClient.reserveBalance(any(BalanceReservationRequest.class)))
                                .thenReturn(new ApiResponse<>(true, "balance reserved", balanceResponse));
                when(userClient.commitBalanceReservation(balanceResponse.getReservationId()))
                                .thenReturn(new ApiResponse<>(true, "committed reservation", null));
                when(bidRepository.save(any(Bid.class))).thenReturn(expectedBid);
                when(bidRepository.findTopByItemIdAndCreatorIdOrderByCreatedDesc(itemId, userId))
                                .thenReturn(Optional.of(usersPreviousBid));
                when(idempotentKeyRepository.findById(idempotentKey)).thenReturn(Optional.of(
                                IdempotentKeys.builder()
                                                .bidAmount(bidAmount)
                                                .bidStatus(BidStatus.PENDING)
                                                .creatorId(userId)
                                                .itemId(itemId)
                                                .build()));

                when(idempotentKeyRepository.save(any(IdempotentKeys.class)))
                                .thenReturn(idempotentKeyEntity);

                when(sagaLogsRepository.save(any(SagaLogs.class)))
                                .thenReturn(sagaLog);

                doNothing().when(sagaLogsRepository).updateStatus(any(UUID.class), any(SagaState.class));

                doNothing().when(bidEventPublisher).sendBidEventToQueue(any(BidEventMessageQueuePayload.class));

                BidDTO result = bidService.placeBid(itemId.toString(), bidAmount, userId.toString(),
                                idempotentKey.toString());

                assertNotNull(result);
                assertEquals(expectedBid.getPrice(), result.getPrice());

                verify(userClient, atLeastOnce()).getUserDetails(userId.toString());
                verify(itemClient, atLeastOnce()).getItem(itemId.toString());
                verify(bidRepository).findTopByItemIdOrderByCreatedDesc(itemId);
                verify(userClient).reserveBalance(any(BalanceReservationRequest.class));
                verify(userClient).commitBalanceReservation(balanceResponse.getReservationId());
                verify(sagaLogsRepository, times(4)).updateStatus(any(UUID.class), any(SagaState.class));
                verify(bidEventPublisher, times(1)).sendBidEventToQueue(any(BidEventMessageQueuePayload.class));
                verify(bidRepository).save(any(Bid.class));
        }

        @Test
        void testPlaceBid_InsufficientBalance() {

                BigDecimal bidAmount = BigDecimal.valueOf(17);
                BigDecimal userBalance = BigDecimal.valueOf(15);
                BigDecimal lastBidAmount = BigDecimal.valueOf(11);
                BigDecimal itemBasePrice = BigDecimal.valueOf(10);

             //   UserDTO user = createUserDTO(userId,userBalance);
                UserDTO previoudUserWhoBid=createUserDTO(previousBidUserId, userBalance);
                ItemDTO item = createItemDTO(itemBasePrice, Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
                Bid lastBid = createBid(lastBidAmount, previousBidUserId);

                // when(userClient.getUserDetails(userId.toString()))
                //                 .thenReturn(new ApiResponse<>(true, "user fetched", user));
                when(userClient.getUserDetails(previousBidUserId.toString()))
                                .thenReturn(new ApiResponse<>(true, "user created", previoudUserWhoBid));
                when(itemClient.getItem(itemId.toString())).thenReturn(item);
                when(itemClient.getItem(itemId.toString())).thenReturn(item);
                when(bidRepository.findTopByItemIdOrderByCreatedDesc(itemId))
                                .thenReturn(Optional.of(lastBid));
                when(bidRepository.findTopByItemIdAndCreatorIdOrderByCreatedDesc(itemId, userId))
                                .thenReturn(Optional.empty());
                when(idempotentKeyRepository.findById(idempotentKey)).thenReturn(Optional.of(
                                IdempotentKeys.builder()
                                                .bidAmount(bidAmount)
                                                .bidStatus(BidStatus.PENDING)
                                                .creatorId(userId)
                                                .itemId(itemId)
                                                .build()));
                IdempotentKeys idempotentKeyEntity = IdempotentKeys.builder()
                                .id(idempotentKey)
                                .bidAmount(bidAmount)
                                .bidStatus(BidStatus.PENDING)
                                .creatorId(userId)
                                .itemId(itemId)
                                .build();
                SagaLogs sagaLog = SagaLogs.builder()
                                .sagaId(UUID.randomUUID())
                                .itemId(itemId)
                                .userId(userId)
                                .currentState(SagaState.STARTED)
                                .bidAmount(bidAmount)
                                .build();
                when(idempotentKeyRepository.save(any(IdempotentKeys.class)))
                                .thenReturn(idempotentKeyEntity);

                when(sagaLogsRepository.save(any(SagaLogs.class)))
                                .thenReturn(sagaLog);

                doNothing().when(sagaLogsRepository).updateStatus(any(UUID.class), any(SagaState.class));

                when(sagaLogsRepository
                                .findById(any(UUID.class))).thenReturn(Optional.of(
                                                SagaLogs.builder().currentState(SagaState.STARTED)
                                                                .bidAmount(bidAmount)
                                                                .itemId(itemId)
                                                                .sagaId(UUID.randomUUID())
                                                                .userId(userId).build()));
                FeignException feignException = new FeignException.BadRequest(
                                "Bad Request from user-service",
                                Request.create(Request.HttpMethod.POST, "/reserve", Map.of(), null, null, null),
                                null, null);
                when(userClient.reserveBalance(any(BalanceReservationRequest.class)))
                                .thenThrow(feignException);

                assertThrows(RuntimeException.class,
                                () -> bidService.placeBid(itemId.toString(), bidAmount, userId.toString(),
                                                idempotentKey.toString()));

                verify(bidRepository, never()).save(any(Bid.class));
                verify(userClient, never()).commitBalanceReservation(anyString());
        }

        @Test
        void testPlaceBid_LowerThanLastBid() {
                BigDecimal bidAmount = BigDecimal.valueOf(10);
                 BigDecimal userBalance = BigDecimal.valueOf(15);
                BigDecimal lastBidAmount = BigDecimal.valueOf(11);
                BigDecimal itemBasePrice = BigDecimal.valueOf(10);

                ItemDTO item = createItemDTO(itemBasePrice, Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
                Bid lastBid = createBid(lastBidAmount, previousBidUserId);
               // UserDTO user = createUserDTO(userId,userBalance);
                UserDTO previoudUserWhoBid=createUserDTO(previousBidUserId, userBalance);
                // when(userClient.getUserDetails(userId.toString()))
                //                 .thenReturn(new ApiResponse<>(true, "user fetched", user));
                when(userClient.getUserDetails(previousBidUserId.toString()))
                                .thenReturn(new ApiResponse<>(true, "user created", previoudUserWhoBid));

                when(itemClient.getItem(itemId.toString())).thenReturn(item);
                when(bidRepository.findTopByItemIdOrderByCreatedDesc(itemId))
                                .thenReturn(Optional.of(lastBid));
                // when(bidRepository.findTopByItemIdAndCreatorIdOrderByCreatedDesc(itemId, userId))
                //                 .thenReturn(Optional.empty());
                when(idempotentKeyRepository.findById(idempotentKey)).thenReturn(Optional.of(
                                IdempotentKeys.builder()
                                                .bidAmount(bidAmount)
                                                .bidStatus(BidStatus.PENDING)
                                                .creatorId(userId)
                                                .itemId(itemId)
                                                .build()));

                InvalidBidException exception = assertThrows(InvalidBidException.class,
                                () -> bidService.placeBid(itemId.toString(), bidAmount, userId.toString(),
                                                idempotentKey.toString()));

                assertTrue(exception.getMessage().contains("Bid price must be higher"));
                verify(bidRepository, never()).save(any(Bid.class));
                verify(userClient, never()).reserveBalance(any(BalanceReservationRequest.class));
        }

        @Test
        void testPlaceBid_LowerThanBasePrice() {
                BigDecimal bidAmount = BigDecimal.valueOf(9);
                BigDecimal itemBasePrice = BigDecimal.valueOf(10);

                ItemDTO item = createItemDTO(itemBasePrice, Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));

                when(idempotentKeyRepository.findById(idempotentKey)).thenReturn(Optional.of(
                                IdempotentKeys.builder()
                                                .bidAmount(bidAmount)
                                                .bidStatus(BidStatus.PENDING)
                                                .creatorId(userId)
                                                .itemId(itemId)
                                                .build()));

                when(itemClient.getItem(itemId.toString())).thenReturn(item);
                when(bidRepository.findTopByItemIdOrderByCreatedDesc(itemId))
                                .thenReturn(Optional.empty());

                // UserDTO user = createUserDTO(userId,BigDecimal.valueOf(100.00));
                // UserDTO previoudUserWhoBid=createUserDTO(previousBidUserId, BigDecimal.valueOf(100.00));

                //  when(userClient.getUserDetails(userId.toString()))
                //                 .thenReturn(new ApiResponse<>(true, "user fetched", user));
                // when(userClient.getUserDetails(previousBidUserId.toString()))
                //                 .thenReturn(new ApiResponse<>(true, "user created", previoudUserWhoBid));

                BidLessThanItemException exception = assertThrows(BidLessThanItemException.class,
                                () -> bidService.placeBid(itemId.toString(), bidAmount, userId.toString(),
                                                idempotentKey.toString()));

                assertTrue(exception.getMessage().contains("Price must be higher than the item's base price"));
                verify(bidRepository, never()).save(any(Bid.class));
                verify(userClient, never()).reserveBalance(any(BalanceReservationRequest.class));
        }

        @Test
        void testPlaceBid_ItemExpired() {
                BigDecimal bidAmount = BigDecimal.valueOf(15);
                BigDecimal itemBasePrice = BigDecimal.valueOf(10);

                // Item expired 1 hour ago
                ItemDTO item = createItemDTO(itemBasePrice, Date.from(Instant.now().minus(1, ChronoUnit.HOURS)));

                //UserDTO user = createUserDTO(userId,BigDecimal.valueOf(100.00));
                //UserDTO previoudUserWhoBid=createUserDTO(previousBidUserId, BigDecimal.valueOf(100.00));

                // when(userClient.getUserDetails(userId.toString()))
                //                 .thenReturn(new ApiResponse<>(true, "user fetched", user));
                // when(userClient.getUserDetails(previousBidUserId.toString()))
                //                 .thenReturn(new ApiResponse<>(true, "user created", previoudUserWhoBid));

                when(idempotentKeyRepository.findById(idempotentKey)).thenReturn(Optional.of(
                                IdempotentKeys.builder()
                                                .bidAmount(bidAmount)
                                                .bidStatus(BidStatus.PENDING)
                                                .creatorId(userId)
                                                .itemId(itemId)
                                                .build()));

                when(itemClient.getItem(itemId.toString())).thenReturn(item);
                when(bidRepository.findTopByItemIdOrderByCreatedDesc(itemId))
                                .thenReturn(Optional.empty());

                ItemExpiredException exception = assertThrows(ItemExpiredException.class,
                                () -> bidService.placeBid(itemId.toString(), bidAmount, userId.toString(),
                                                idempotentKey.toString()));

                assertTrue(exception.getMessage().contains("has expired"));
                verify(bidRepository, never()).save(any(Bid.class));
                verify(userClient, never()).reserveBalance(any(BalanceReservationRequest.class));
        }

        @Test
        void testPlaceBid_ItemNotFound() {
                
                BigDecimal bidAmount = BigDecimal.valueOf(15);
                            // UserDTO user = createUserDTO(userId,userBalance);
                UserDTO previoudUserWhoBid=createUserDTO(previousBidUserId, BigDecimal.valueOf(50.00));
                
                 FeignException feignException = new FeignException.BadRequest(
                                "Bad Request from item-service",
                                Request.create(Request.HttpMethod.GET, "/details", Map.of(), null, null, null),
                                null, null);
                when(itemClient.getItem(any(String.class)))
                                .thenThrow(feignException);

                                              Bid lastBid = createBid(BigDecimal.valueOf(16.00), previousBidUserId);
  

                when(bidRepository.findTopByItemIdOrderByCreatedDesc(itemId))
                                .thenReturn(Optional.of(lastBid));
                when(userClient.getUserDetails(previousBidUserId.toString()))
                                .thenReturn(new ApiResponse<>(true, "user created", previoudUserWhoBid));

                assertThrows(RuntimeException.class,
                                () -> bidService.placeBid(itemId.toString(), bidAmount, userId.toString(),
                                                idempotentKey.toString()));


            
                verify(bidRepository, never()).save(any(Bid.class));
                verify(userClient, never()).reserveBalance(any(BalanceReservationRequest.class));

        
             

        }

        @Test

        void testGetBidsOfItems_ReturnsListOfBids() {

                Bid bid1 = createBid(BigDecimal.valueOf(10.5), previousBidUserId);
                Bid bid2 = createBid(BigDecimal.valueOf(12.0), userId);
                List<Bid> mockBids = List.of(bid1, bid2);

                when(bidRepository.findByItemIdOrderByCreatedDesc(itemId)).thenReturn(mockBids);

                List<Bid> result = bidService.getBidsOfItems(itemId.toString());
                assertNotNull(result);
                assertEquals(2, result.size());
                assertEquals(BigDecimal.valueOf(10.5), result.get(0).getPrice());
                assertEquals(BigDecimal.valueOf(12.0), result.get(1).getPrice());
                verify(bidRepository, times(1)).findByItemIdOrderByCreatedDesc(itemId);
        }

        @Test
        void testGetBidsOfItems_ReturnsEmptyList() {

                when(bidRepository.findByItemIdOrderByCreatedDesc(itemId)).thenReturn(List.of());

                List<Bid> result = bidService.getBidsOfItems(itemId.toString());

                assertNotNull(result);
                assertTrue(result.isEmpty());
                verify(bidRepository, times(1)).findByItemIdOrderByCreatedDesc(itemId);
        }

        

        private UserDTO createUserDTO(UUID userId,BigDecimal balance) {
                return UserDTO.builder()
                                .id(userId)
                                .age(20)
                                .email(USER_EMAIL)
                                .balance(balance)
                                .profilePicture(USER_PROFILE_PICTURE)
                                .role(Roles.CUSTOMER)
                                .name(USER_NAME)
                                .build();
        }

        private ItemDTO createItemDTO(BigDecimal basePrice, Date expiryTime) {
                return ItemDTO.builder()
                                .id(itemId)
                                .basePrice(basePrice)
                                .brand(ITEM_BRAND)
                                .deleted(false)
                                .title(ITEM_NAME)
                                .createdAt(Instant.now())
                                .description(ITEM_DESCRIPTION)
                                .expiryTime(expiryTime)
                                .categoryId(categoryId)
                                .creatorUserId(sellerId)
                                .updatedAt(Instant.now())
                                .pictureIds(PICTURE_IDS)
                                .build();
        }

        private Bid createBid(BigDecimal price, UUID userId) {
                return Bid.builder()
                                .id(UUID.randomUUID())
                                .created(Instant.now())
                                .updated(Instant.now())
                                .itemId(itemId)
                                .creatorId(userId)
                                .price(price)
                                .build();
        }

        private BalanceReservationResponse createBalanceReservationResponse() {
                return new BalanceReservationResponse(
                                UUID.randomUUID().toString(),
                                userId.toString(),
                                IDEMPOTENT_KEY);
        }
}