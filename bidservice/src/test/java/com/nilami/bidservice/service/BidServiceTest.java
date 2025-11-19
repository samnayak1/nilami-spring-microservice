package com.nilami.bidservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
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
import com.nilami.bidservice.dto.ItemDTO;
import com.nilami.bidservice.dto.Roles;
import com.nilami.bidservice.dto.UserDTO;
import com.nilami.bidservice.exceptions.BidPlacementException;

import com.nilami.bidservice.models.Bid;
import com.nilami.bidservice.repositories.BidRepository;
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
    private UserClient userClient;

    @Mock
    private ItemClient itemClient;

    @Mock
   private  BidEventPublisher bidEventPublisher;



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

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        userId = UUID.randomUUID();
        sellerId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
    }


    @Test

    void testPlaceBid_Success() throws Exception {
      
        BigDecimal bidAmount = BigDecimal.valueOf(12);
        BigDecimal userBalance = BigDecimal.valueOf(30);
        BigDecimal lastBidAmount = BigDecimal.valueOf(11);
        BigDecimal itemBasePrice = BigDecimal.valueOf(10);

        UserDTO user = createUserDTO(userBalance);
        ItemDTO item = createItemDTO(itemBasePrice);
        Bid lastBid = createBid(lastBidAmount);
        Bid expectedBid = createBid(bidAmount);
        BalanceReservationResponse balanceResponse = createBalanceReservationResponse();

        when(userClient.getUserDetails(userId.toString()))
                .thenReturn(new ApiResponse<>(true, "user created", user));
        when(itemClient.getItem(itemId.toString())).thenReturn(item);
        when(bidRepository.findTopByItemIdOrderByCreatedDesc(itemId))
                .thenReturn(Optional.of(lastBid));
        when(userClient.reserveBalance(any(BalanceReservationRequest.class)))
                .thenReturn(new ApiResponse<>(true, "balance reserved", balanceResponse));
        when(userClient.commitBalanceReservation(balanceResponse.getReservationId()))
                .thenReturn(new ApiResponse<>(true, "committed reservation", null));
        when(bidRepository.save(any(Bid.class))).thenReturn(expectedBid);
        
 
        BidDTO result = bidService.placeBid(itemId.toString(), bidAmount, userId.toString());


        assertNotNull(result);
        assertEquals(expectedBid.getPrice(), result.getPrice());
        
        verify(userClient, atLeastOnce()).getUserDetails(userId.toString());
        verify(itemClient, atLeastOnce()).getItem(itemId.toString());
        verify(bidRepository).findTopByItemIdOrderByCreatedDesc(itemId);
        verify(userClient).reserveBalance(any(BalanceReservationRequest.class));
        verify(userClient).commitBalanceReservation(balanceResponse.getReservationId());
        verify(bidRepository).save(any(Bid.class));
    }

    @Test
    void testPlaceBid_InsufficientBalance() {
       
        BigDecimal bidAmount = BigDecimal.valueOf(17);
        BigDecimal userBalance = BigDecimal.valueOf(15);
        BigDecimal lastBidAmount = BigDecimal.valueOf(11);
        BigDecimal itemBasePrice = BigDecimal.valueOf(10);

        UserDTO user = createUserDTO(userBalance);
        ItemDTO item = createItemDTO(itemBasePrice);
        Bid lastBid = createBid(lastBidAmount);

        when(userClient.getUserDetails(userId.toString()))
                .thenReturn(new ApiResponse<>(true, "user fetched", user));
        when(itemClient.getItem(itemId.toString())).thenReturn(item);
        when(bidRepository.findTopByItemIdOrderByCreatedDesc(itemId))
                .thenReturn(Optional.of(lastBid));

        FeignException feignException = new FeignException.BadRequest(
                "Bad Request from user-service",
                Request.create(Request.HttpMethod.POST, "/reserve", Map.of(), null, null, null),
                null, null);
        when(userClient.reserveBalance(any(BalanceReservationRequest.class)))
                .thenThrow(feignException);

        assertThrows(RuntimeException.class,
                () -> bidService.placeBid(itemId.toString(), bidAmount, userId.toString()));

        verify(bidRepository, never()).save(any(Bid.class));
        verify(userClient, never()).commitBalanceReservation(anyString());
    }

    @Test
    void testPlaceBid_LowerThanLastBid() {
    
        BigDecimal bidAmount = BigDecimal.valueOf(10);
        BigDecimal userBalance = BigDecimal.valueOf(15);
        BigDecimal lastBidAmount = BigDecimal.valueOf(11);
        BigDecimal itemBasePrice = BigDecimal.valueOf(10);

        UserDTO user = createUserDTO(userBalance);
        ItemDTO item = createItemDTO(itemBasePrice);
        Bid lastBid = createBid(lastBidAmount);

        when(userClient.getUserDetails(userId.toString()))
                .thenReturn(new ApiResponse<>(true, "user fetched", user));
        when(itemClient.getItem(itemId.toString())).thenReturn(item);
        when(bidRepository.findTopByItemIdOrderByCreatedDesc(itemId))
                .thenReturn(Optional.of(lastBid));

        BidPlacementException exception = assertThrows(BidPlacementException.class,
                () -> bidService.placeBid(itemId.toString(), bidAmount, userId.toString()));

        assertTrue(exception.getMessage().contains("Bid price must be higher"));
        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    void testPlaceBid_LowerThanBasePrice() {
       
        BigDecimal bidAmount = BigDecimal.valueOf(9);
        BigDecimal itemBasePrice = BigDecimal.valueOf(10);

        ItemDTO item = createItemDTO(itemBasePrice);

        when(itemClient.getItem(itemId.toString())).thenReturn(item);

        BidPlacementException exception = assertThrows(BidPlacementException.class,
                () -> bidService.placeBid(itemId.toString(), bidAmount, userId.toString()));

        assertTrue(exception.getMessage().contains("Price must be higher than the item's base price"));
        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test

    void testGetBidsOfItems_ReturnsListOfBids() {
     
        Bid bid1 = createBid(BigDecimal.valueOf(10.5));
        Bid bid2 = createBid(BigDecimal.valueOf(12.0));
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


    private UserDTO createUserDTO(BigDecimal balance) {
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

    private ItemDTO createItemDTO(BigDecimal basePrice) {
        return ItemDTO.builder()
                .id(itemId)
                .basePrice(basePrice)
                .brand(ITEM_BRAND)
                .deleted(false)
                .title(ITEM_NAME)
                .createdAt(Instant.now())
                .description(ITEM_DESCRIPTION)
                .expiryTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .categoryId(categoryId)
                .creatorUserId(sellerId)
                .updatedAt(Instant.now())
                .pictureIds(PICTURE_IDS)
                .build();
    }

    private Bid createBid(BigDecimal price) {
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