package com.nilami.bidservice.controllers.v1;

import java.util.List;


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

import com.nilami.bidservice.models.Bid;

import com.nilami.bidservice.services.BidService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
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
