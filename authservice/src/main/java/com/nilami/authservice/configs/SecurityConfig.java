package com.nilami.authservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   HeaderAuthenticationFilter headerAuthFilter)
            throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/api/auth/v1/signup",
                    "/api/auth/v1/validate-token",
                    "/api/auth/v1/login",
                    "/api/auth/v1/refresh",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/prometheus", 
                    "/actuator/health/**", 
                    "/actuator/info",
                    "/api/auth/v1/payment/webhook"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}