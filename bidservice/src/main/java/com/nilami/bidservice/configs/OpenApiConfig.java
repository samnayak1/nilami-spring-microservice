package com.nilami.bidservice.configs;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;

import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
   @Value("${gateway.url:http://localhost:8084}")
    private String gatewayUrl;

    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .servers(List.of(
                new Server()
                    .url(gatewayUrl)
                    .description("Bid Service")
            ));
}
}