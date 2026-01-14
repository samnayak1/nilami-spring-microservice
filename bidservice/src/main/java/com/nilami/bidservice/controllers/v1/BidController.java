package com.nilami.bidservice.controllers.v1;

import java.math.BigDecimal;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nilami.bidservice.controllers.requestTypes.PlaceBidRequest;
import com.nilami.bidservice.dto.ApiResponse;
import com.nilami.bidservice.dto.BidDTO;

import com.nilami.bidservice.dto.GetBidsOfUserWithItemDetails;
import com.nilami.bidservice.dto.GetHighestBidsRequest;
import com.nilami.bidservice.dto.GetIdempotentKeyRequest;
import com.nilami.bidservice.dto.GetIdempotentKeyResponse;
import com.nilami.bidservice.models.Bid;

import com.nilami.bidservice.services.BidService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
@Slf4j
public class BidController {

    private final BidService bidService;

    @GetMapping("/test")
    public ResponseEntity<String> testController(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles) {

        System.out.println("roles: " + roles);
        System.out.println("userId: " + userId);

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
            @Parameter(hidden = true)  @RequestHeader("X-User-Id") String userId,
            @Parameter(hidden = true) @RequestHeader("X-User-Roles") String roles) {
        try {

            BidDTO placedBid = bidService.placeBid(request.getItemId(), request.getPrice(), userId,
                    request.getIdempotentKey());

            return ResponseEntity.ok(new ApiResponse<>(true, "Bids placed successfully", placedBid));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error placing bid: " + e.getMessage(), null));
        }

    }

    @GetMapping("/all/user")
    public ResponseEntity<ApiResponse<List<GetBidsOfUserWithItemDetails>>> getAllBidsOfUser(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {

        try {

            List<GetBidsOfUserWithItemDetails> bids = bidService.getBidsOfUserAlongWithHighestBidForItem(userId);

            return ResponseEntity.ok(new ApiResponse<>(true, "Bids fstched successfully", bids));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error fetching bids: " + e.getMessage(), null));
        }
    }

    @PostMapping("/idempotent")
    public ResponseEntity<ApiResponse<GetIdempotentKeyResponse>> getIdempotentKey(
            @RequestBody GetIdempotentKeyRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {

        try {

            GetIdempotentKeyResponse idempotentKeyResponse = bidService.getIdempotentKey(
                    request.getItemId(),
                    request.getBidAmount(),
                    userId
                );
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Idempotent successfully",
                    idempotentKeyResponse));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<GetIdempotentKeyResponse>(
                            false, "Error fetching bids: " + e.getMessage(),
                            null));
        }

    }

    @PostMapping("/highest-bids")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getHighestBidsForItems(
            @RequestBody GetHighestBidsRequest request) {
        
        log.debug("Received request to get highest bids for items: {}", request.getItemIds());
        
        try {
            Map<String, BigDecimal> highestBids = bidService.getItemsHighestBidGivenItemIds(request.getItemIds());
            
            return ResponseEntity.ok(new ApiResponse<Map<String,BigDecimal>>(true, "ItemId to highest Bid Price Map", highestBids));
                    
        } catch (Exception e) {
            log.error("Error fetching highest bids for items: {}", request.getItemIds(), e);
            
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error fetching itemId to bid map: " + e.getMessage(), null));
        }
    }
}


