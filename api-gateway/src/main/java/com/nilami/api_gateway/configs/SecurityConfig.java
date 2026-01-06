package com.nilami.api_gateway.configs;

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

    private final AuthServiceForwardHeaderFilter authFilter;

    public SecurityConfig(AuthServiceForwardHeaderFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/gateway/test",
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/test",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/validate-token",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/*/v3/api-docs/**",
                                "/auth/v3/api-docs/**", 
                                "/catalog/v3/api-docs/**", 
                                "/bid/v3/api-docs/**",
                                "/actuator/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
