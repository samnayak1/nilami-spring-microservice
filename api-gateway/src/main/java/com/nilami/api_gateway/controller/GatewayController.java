package com.nilami.api_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/gateway/v1")
public class GatewayController {


 @GetMapping("/test")
    public Mono<ResponseEntity<String>> testController() {
        return Mono.just(ResponseEntity.ok("Hello"));
    }
}
