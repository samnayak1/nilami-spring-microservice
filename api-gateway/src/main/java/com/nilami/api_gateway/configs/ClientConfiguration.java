package com.nilami.api_gateway.configs;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfiguration {

    @Bean

    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}