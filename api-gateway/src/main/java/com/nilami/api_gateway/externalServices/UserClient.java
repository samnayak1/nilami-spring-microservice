package com.nilami.api_gateway.externalServices;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import org.springframework.web.reactive.function.client.WebClient;

import com.nilami.dto.TokenValidationRequest;
import com.nilami.dto.TokenValidationResponse;

import reactor.core.publisher.Mono;

@Component
public class UserClient {

    private final WebClient webClient;

    public UserClient(WebClient.Builder webClientBuilder, 
                      @Value("${AUTH_SERVICE_HOST}") String baseUrl) {
      
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<TokenValidationResponse> validateToken(TokenValidationRequest request) {
        return this.webClient.post()
                .uri("/api/auth/v1/validate-token")
                .bodyValue(request)
                .retrieve()
            
                .onStatus(HttpStatusCode::isError, response -> 
                    Mono.error(new RuntimeException("Auth Service Error")))
                .bodyToMono(TokenValidationResponse.class);
    }
}
