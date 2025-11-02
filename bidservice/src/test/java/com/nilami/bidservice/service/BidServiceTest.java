package com.nilami.bidservice.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import java.util.Date;
import java.util.List;
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
import com.nilami.bidservice.models.Bid;
import com.nilami.bidservice.repositories.BidRepository;
import com.nilami.bidservice.services.bidServiceImplementations.BidServiceImplementation;
import com.nilami.bidservice.services.externalClients.ItemClient;
import com.nilami.bidservice.services.externalClients.UserClient;

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
                .thenReturn(Optional.of(new Bid(randomBidId, Instant.now(), Instant.now(), randomItemId, randomUserId,
                        BigDecimal.valueOf(2.0))));

        when(itemClient.getItem(randomItemId.toString()))
                .thenReturn(item);

        when(userClient.reserveBalance(any(BalanceReservationRequest.class)))
                .thenReturn(new ApiResponse<>(true, "balance reserved", balanceResponse));

        // when(userClient.cancelBalanceReservation(randomReservationId))
        //         .thenReturn(new ApiResponse<Void>(true, randomReservationId, null));

        
        when(userClient.commitBalanceReservation(randomReservationId))
        .thenReturn(new ApiResponse<Void>(true, "commited reservation", null));
        
        when(bidRepository.save(any(Bid.class)))
                .thenReturn(expectedBid);


        BidDTO bid = bidService.placeBid(randomItemId.toString(), placedBidAmount, randomUserId.toString());

        assertNotNull(bid);

    }

    // TODO: when user has insufficient funds

    // TODO: when bid less than previous bid

    // TODO: when bid is less than base price

    // TODO: get all bids

}
