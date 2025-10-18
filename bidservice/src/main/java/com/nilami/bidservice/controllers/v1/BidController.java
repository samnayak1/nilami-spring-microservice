package com.nilami.bidservice.controllers.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bids")
public class BidController {

     @GetMapping("/test")
    public ResponseEntity<String> testController(   
                              @RequestHeader("X-User-Id") String userId,
                              @RequestHeader("X-User-Roles") String roles) {
        
        System.out.println("roles: "+roles);
        System.out.println("userId: "+userId);
        return ResponseEntity.ok("Hello");
    }


}
