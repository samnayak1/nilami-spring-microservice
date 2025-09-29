package com.nilami.api_gateway.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;

import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable) // disable CSRF for APIs
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(
                    "/api/v1/gateway/login",
                    "/api/v1/gateway/test",
                    "/api/v1/gateway/signup"
                ).permitAll()          // make these routes public
                .anyExchange().authenticated() // secure everything else
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }

    
}
