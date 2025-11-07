package com.nilami.bidservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import com.nilami.bidservice.services.bidServiceImplementations.BidServiceImplementation;
import com.nilami.bidservice.services.externalClients.ItemClient;
import com.nilami.bidservice.services.externalClients.UserClient;

import feign.FeignException;
import feign.Request;

@ExtendWith(MockitoExtension.class)
public class BidServiceTest {

        @Mock
        private BidRepository bidRepository;

        @Mock
        private UserClient userClient;

        @Mock
        private ItemClient itemClient;

        @InjectMocks
        private BidServiceImplementation bidService;

        @Test
        void testPlaceBid_ExpectBidReturned() throws Exception {
                UUID randomItemId = UUID.randomUUID();
                UUID randomBidId = UUID.randomUUID();
                UUID randomUserId = UUID.randomUUID();
                UUID randomCategoryId = UUID.randomUUID();

                UUID randomSellerId = UUID.randomUUID();

                String userEmail = new String("example@email.com");

                String userProfilePicture = new String("https://www.example.fgk/pic1");

                String userName = new String("Ange Postecoglu");

                String randomItemBrand = new String("Nike");

                String randomItemName = new String("Antique Shoes");

                String randomItemDesciption = new String("Shoes worn in the 18th Century");

                String idempotentKey = new String("idempotentKey456");

                String randomReservationId = UUID.randomUUID().toString();

                List<String> randomPictureIds = List.of("pic1", "pic2", "pic3");

                UUID bidId = UUID.randomUUID();

                BigDecimal placedBidAmount = BigDecimal.valueOf(12);

                UserDTO user = UserDTO.builder()
                                .id(randomUserId)
                                .age(20)
                                .email(userEmail)
                                .balance(BigDecimal.valueOf(30))
                                .profilePicture(userProfilePicture)
                                .role(Roles.CUSTOMER)
                                .name(userName)
                                .build();

                ItemDTO item = ItemDTO.builder()
                                .id(randomItemId)
                                .basePrice(BigDecimal.valueOf(10))
                                .brand(randomItemBrand)
                                .deleted(false)
                                .title(randomItemName)
                                .createdAt(Instant.now())
                                .description(randomItemDesciption)
                                .expiryTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                                .categoryId(randomCategoryId)
                                .creatorUserId(randomSellerId)
                                .updatedAt(Instant.now())
                                .pictureIds(randomPictureIds)
                                .build();

                Bid expectedBid = Bid.builder()
                                .id(bidId)
                                .created(Instant.now())
                                .creatorId(randomUserId)
                                .price(placedBidAmount)
                                .itemId(randomItemId)
                                .build();

                BalanceReservationResponse balanceResponse = new BalanceReservationResponse(randomReservationId,
                                randomUserId.toString(), idempotentKey);

                when(userClient.getUserDetails(randomUserId.toString()))
                                .thenReturn(new ApiResponse<UserDTO>(true, "user created", user));

                when(bidRepository.findTopByItemIdOrderByCreatedDesc(randomItemId))
                                .thenReturn(Optional.of(new Bid(randomBidId, Instant.now(), Instant.now(), randomItemId,
                                                randomUserId,
                                                BigDecimal.valueOf(11.0))));

                when(itemClient.getItem(randomItemId.toString()))
                                .thenReturn(item);

                when(userClient.reserveBalance(any(BalanceReservationRequest.class)))
                                .thenReturn(new ApiResponse<>(true, "balance reserved", balanceResponse));

                when(userClient.commitBalanceReservation(randomReservationId))
                                .thenReturn(new ApiResponse<Void>(true, "commited reservation", null));

                when(bidRepository.save(any(Bid.class)))
                                .thenReturn(expectedBid);

                BidDTO bid = bidService.placeBid(randomItemId.toString(), placedBidAmount, randomUserId.toString());

                assertNotNull(bid);

                assertEquals(bid.getPrice(), expectedBid.getPrice());

        }

        @Test
        void testPlaceBid_shouldThrowException_whenUserBalanceLessThanBidPrice() throws Exception {
                UUID randomItemId = UUID.randomUUID();
                UUID randomBidId = UUID.randomUUID();
                UUID randomUserId = UUID.randomUUID();
                UUID randomCategoryId = UUID.randomUUID();

                UUID randomSellerId = UUID.randomUUID();

                String userEmail = new String("example@email.com");

                String userProfilePicture = new String("https://www.example.fgk/pic1");

                String userName = new String("Ange Postecoglu");

                String randomItemBrand = new String("Nike");

                String randomItemName = new String("Antique Shoes");

                String randomItemDesciption = new String("Shoes worn in the 18th Century");

                List<String> randomPictureIds = List.of("pic1", "pic2", "pic3");

                BigDecimal placedBidAmount = BigDecimal.valueOf(17);

                UserDTO user = UserDTO.builder()
                                .id(randomUserId)
                                .age(20)
                                .email(userEmail)
                                .balance(BigDecimal.valueOf(15))
                                .profilePicture(userProfilePicture)
                                .role(Roles.CUSTOMER)
                                .name(userName)
                                .build();

                ItemDTO item = ItemDTO.builder()
                                .id(randomItemId)
                                .basePrice(BigDecimal.valueOf(10))
                                .brand(randomItemBrand)
                                .deleted(false)
                                .title(randomItemName)
                                .createdAt(Instant.now())
                                .description(randomItemDesciption)
                                .expiryTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                                .categoryId(randomCategoryId)
                                .creatorUserId(randomSellerId)
                                .updatedAt(Instant.now())
                                .pictureIds(randomPictureIds)
                                .build();

                when(userClient.getUserDetails(randomUserId.toString()))
                                .thenReturn(new ApiResponse<UserDTO>(true, "user created", user));

                when(bidRepository.findTopByItemIdOrderByCreatedDesc(randomItemId))
                                .thenReturn(Optional.of(new Bid(randomBidId, Instant.now(), Instant.now(), randomItemId,
                                                randomUserId,
                                                BigDecimal.valueOf(11.0))));

                when(itemClient.getItem(randomItemId.toString()))
                                .thenReturn(item);

                FeignException feignException = new FeignException.BadRequest(
                                "Bad Request from user-service",
                                Request.create(Request.HttpMethod.POST, "/reserve", Map.of(), null, null, null),
                                null,
                                null);
                when(userClient.reserveBalance(any(BalanceReservationRequest.class)))
                                .thenThrow(feignException);

                assertThrows(RuntimeException.class, () -> bidService.placeBid(randomItemId.toString(), placedBidAmount,
                                randomUserId.toString()));

        }

        @Test
        void testPlaceBid_shouldThrowBidPlacementException_WhenBidLowerThanLastBid() throws Exception {

                UUID randomItemId = UUID.randomUUID();
                UUID randomBidId = UUID.randomUUID();
                UUID randomUserId = UUID.randomUUID();
                UUID randomCategoryId = UUID.randomUUID();

                UUID randomSellerId = UUID.randomUUID();

                String userEmail = new String("example@email.com");

                String userProfilePicture = new String("https://www.example.fgk/pic1");

                String userName = new String("Ange Postecoglu");

                String randomItemBrand = new String("Nike");

                String randomItemName = new String("Antique Shoes");

                String randomItemDesciption = new String("Shoes worn in the 18th Century");

                List<String> randomPictureIds = List.of("pic1", "pic2", "pic3");

                BigDecimal placedBidAmount = BigDecimal.valueOf(10);

                UserDTO user = UserDTO.builder()
                                .id(randomUserId)
                                .age(20)
                                .email(userEmail)
                                .balance(BigDecimal.valueOf(15))
                                .profilePicture(userProfilePicture)
                                .role(Roles.CUSTOMER)
                                .name(userName)
                                .build();

                ItemDTO item = ItemDTO.builder()
                                .id(randomItemId)
                                .basePrice(BigDecimal.valueOf(10))
                                .brand(randomItemBrand)
                                .deleted(false)
                                .title(randomItemName)
                                .createdAt(Instant.now())
                                .description(randomItemDesciption)
                                .expiryTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                                .categoryId(randomCategoryId)
                                .creatorUserId(randomSellerId)
                                .updatedAt(Instant.now())
                                .pictureIds(randomPictureIds)
                                .build();

                when(userClient.getUserDetails(randomUserId.toString()))
                                .thenReturn(new ApiResponse<UserDTO>(true, "user created", user));

                when(bidRepository.findTopByItemIdOrderByCreatedDesc(randomItemId))
                                .thenReturn(Optional.of(new Bid(randomBidId, Instant.now(), Instant.now(), randomItemId,
                                                randomUserId,
                                                BigDecimal.valueOf(11.0))));

                when(itemClient.getItem(randomItemId.toString()))
                                .thenReturn(item);

                BidPlacementException ex = assertThrows(
                                BidPlacementException.class,
                                () -> bidService.placeBid(randomItemId.toString(), placedBidAmount,
                                                randomUserId.toString()));

                assertTrue(ex.getMessage().contains("Bid price must be higher"));
        }

  

                @Test
        void testPlaceBid_shouldThrowBidPlacementException_WhenBidLowerThanItem() throws Exception {

                UUID randomItemId = UUID.randomUUID();
              
                UUID randomUserId = UUID.randomUUID();
                UUID randomCategoryId = UUID.randomUUID();

                UUID randomSellerId = UUID.randomUUID();

                String randomItemBrand = new String("Nike");

                String randomItemName = new String("Antique Shoes");

                String randomItemDesciption = new String("Shoes worn in the 18th Century");

                List<String> randomPictureIds = List.of("pic1", "pic2", "pic3");

                BigDecimal placedBidAmount = BigDecimal.valueOf(9);

            

                ItemDTO item = ItemDTO.builder()
                                .id(randomItemId)
                                .basePrice(BigDecimal.valueOf(10))
                                .brand(randomItemBrand)
                                .deleted(false)
                                .title(randomItemName)
                                .createdAt(Instant.now())
                                .description(randomItemDesciption)
                                .expiryTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                                .categoryId(randomCategoryId)
                                .creatorUserId(randomSellerId)
                                .updatedAt(Instant.now())
                                .pictureIds(randomPictureIds)
                                .build();

       

                when(itemClient.getItem(randomItemId.toString()))
                                .thenReturn(item);

                BidPlacementException ex = assertThrows(
                                BidPlacementException.class,
                                () -> bidService.placeBid(randomItemId.toString(), placedBidAmount,
                                                randomUserId.toString()));

                assertTrue(ex.getMessage().contains("Price must be higher than the item's base price"));
        }
   @Test
    void testGetBidsOfItems_ReturnsListOfBids() {
     
        UUID itemId = UUID.randomUUID();
        String itemIdStr = itemId.toString();

        Bid bid1 = new Bid(UUID.randomUUID(), Instant.now(), Instant.now(),
                itemId, UUID.randomUUID(), BigDecimal.valueOf(10.5));
        Bid bid2 = new Bid(UUID.randomUUID(), Instant.now(), Instant.now(),
                itemId, UUID.randomUUID(), BigDecimal.valueOf(12.0));

        List<Bid> mockBids = List.of(bid1, bid2);

        when(bidRepository.findByItemIdOrderByCreatedDesc(itemId)).thenReturn(mockBids);


        List<Bid> result = bidService.getBidsOfItems(itemIdStr);

   
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(12.0), result.get(1).getPrice());
        verify(bidRepository, times(1)).findByItemIdOrderByCreatedDesc(itemId);
    }

    @Test
    void testGetBidsOfItems_ReturnsEmptyList_WhenNoBidsFound() {
     
        UUID itemId = UUID.randomUUID();
        String itemIdStr = itemId.toString();

        when(bidRepository.findByItemIdOrderByCreatedDesc(itemId)).thenReturn(List.of());

   
        List<Bid> result = bidService.getBidsOfItems(itemIdStr);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bidRepository, times(1)).findByItemIdOrderByCreatedDesc(itemId);
    }



}
