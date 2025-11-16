package com.nilami.bidservice.controllers.v1;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.nilami.bidservice.controllers.requestTypes.PlaceBidRequest;
import com.nilami.bidservice.dto.ApiResponse;
import com.nilami.bidservice.dto.BidDTO;
import com.nilami.bidservice.dto.BidEventMessageQueuePayload;
import com.nilami.bidservice.models.Bid;
import com.nilami.bidservice.services.BidEventPublisher;
import com.nilami.bidservice.services.BidService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
public class BidController {

     private final BidService bidService;

     private final BidEventPublisher bidEventPublisher;


    @GetMapping("/test")
    public ResponseEntity<String> testController(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles) {

        System.out.println("roles: " + roles);
        System.out.println("userId: " + userId);
        bidEventPublisher.sendBidEventToQueue(new BidEventMessageQueuePayload(UUID.fromString("7a6c5a1f-4d8b-45c4-935b-2d0e9db05e8c"), UUID.fromString("f2c3e4be-1c0d-4d75-9d49-0a1be6e0d82a"), BigDecimal.valueOf(89.92), UUID.fromString("3d8f0a57-8b97-4ad1-af49-8f50d6e6f2fe"), Instant.now().toString()));
        return ResponseEntity.ok("Hello");
    }

@GetMapping("/all/{itemId}")
public ResponseEntity<ApiResponse<List<Bid>>> getAllBidsOfItem(
        @PathVariable String itemId) {

    try {
        List<Bid> bids = bidService.getBidsOfItems(itemId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Bids fetched successfully", bids));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching bids: " + e.getMessage(), null));
    }
}
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BidDTO>> placeBid(
            @RequestBody PlaceBidRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles) {
        try {
          
            BidDTO placedBid=bidService.placeBid(request.getItemId(), request.getPrice(), userId);

           return ResponseEntity.ok(new ApiResponse<>(true, "Bids placed successfully", placedBid));
          
            
        } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error placing bid: " + e.getMessage(), null));
        }

    }
}
