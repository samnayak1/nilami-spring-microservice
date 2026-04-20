package com.nilami.bidservice.controllers.v2;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nilami.bidservice.dto.ApiResponse;
import com.nilami.bidservice.dto.GetHighestBidAlongWithItemIds;
import com.nilami.bidservice.dto.GetHighestBidsRequest;
import com.nilami.bidservice.services.BidService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/bids/v2")
@RequiredArgsConstructor
@Slf4j
public class BidV2Controller  {
    private final BidService bidService;



        @PostMapping("/highest-bids")
        @ResponseStatus(HttpStatus.OK)
        public ResponseEntity<ApiResponse<Map<String, GetHighestBidAlongWithItemIds>>> getHighestBidsAlongWithUserIdForItems(
                @RequestBody GetHighestBidsRequest request) {
             
               log.debug("Received request to get highest bids along with user id for items: {}", request.getItemIds());
             
          
                Map<String, GetHighestBidAlongWithItemIds> highestBids = bidService.getHighestBids(request.getItemIds());
                
                return ResponseEntity.ok(
                        new ApiResponse<Map<String,GetHighestBidAlongWithItemIds>>
                        (true, "item id to user id map", highestBids));
    }
}
