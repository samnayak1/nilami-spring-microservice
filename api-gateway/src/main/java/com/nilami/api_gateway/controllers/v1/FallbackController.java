package com.nilami.api_gateway.controllers.v1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class FallbackController {
   @GetMapping("/fallback/bids")
    public String bidServiceFallback() {
        return "Bid Service is currently unavailable. Please try again later.";
    }
      @GetMapping("/fallback/catalog")
    public String catalogServiceFallback() {
        return "Catalog Service is currently unavailable. Please try again later.";
    }
      @GetMapping("/fallback/auth")
    public String authServiceFallback() {
        return "Auth Service is currently unavailable. Please try again later.";
    }
 
}
