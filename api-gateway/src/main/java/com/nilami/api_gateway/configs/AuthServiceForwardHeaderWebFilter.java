package com.nilami.api_gateway.configs;


import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.security.core.Authentication;
import com.nilami.api_gateway.externalServices.UserClient;
import com.nilami.dto.TokenValidationRequest;


import java.util.List;

@Component
public class AuthServiceForwardHeaderWebFilter implements WebFilter {

    private final UserClient authServiceClient;

    public AuthServiceForwardHeaderWebFilter(UserClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @SuppressWarnings("null")
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

       
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            return authServiceClient.validateToken(new TokenValidationRequest(token))
                    .flatMap(validationData -> {
                        if (validationData != null && validationData.isValid()) {


                            List<SimpleGrantedAuthority> authorities = validationData.getUserInfo().getRoles().stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .toList();

                            Authentication auth = new UsernamePasswordAuthenticationToken(
                                    validationData.getUserInfo().getUserId(),
                                    null,
                                    authorities); 
                            
                                    //add header like X-User-Id (the person's userid and X-User-Roles which is the users role separated by ) to the forwarded requesrs
                            ServerWebExchange mutatedExchange = exchange.mutate()
                                    .request(r -> r.header("X-User-Id", validationData.getUserInfo().getUserId())
                                            .header("X-User-Roles",
                                                    String.join(",", validationData.getUserInfo().getRoles())))
                                    .build();


                            return chain.filter(mutatedExchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                    })
                    .onErrorResume(e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        }

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/gateway/test") ||
                path.startsWith("/api/v1/auth/signup") ||
                path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/test") ||
                path.startsWith("/api/v1/auth/refresh") ||
                path.startsWith("/api/v1/auth/validate-token") ||
                path.contains("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api/v1/auth/payment/webhook") ||
                path.matches(".*/swagger-ui.*") ||
                path.startsWith("/actuator");
    }
}