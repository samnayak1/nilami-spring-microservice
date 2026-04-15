package com.nilami.api_gateway.configs;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
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
import com.nilami.api_gateway.service.RateLimiter;
import com.nilami.api_gateway.service.RateLimiterFactory;
import com.nilami.api_gateway.utils.IpExtractor;
import com.nilami.dto.TokenValidationRequest;

import java.util.List;

@Component
public class AuthServiceForwardHeaderWebFilter implements WebFilter {

    private final UserClient authServiceClient;
    private final IpExtractor ipExtractor;
    private final RateLimiter rateLimiter;

    public AuthServiceForwardHeaderWebFilter(
            UserClient authServiceClient,
            IpExtractor ipExtractor,
            RateLimiterFactory rateLimitFactory) {

        this.authServiceClient = authServiceClient;
        this.ipExtractor = ipExtractor;
        this.rateLimiter = rateLimitFactory.get("sliding");
    }

    @SuppressWarnings("null")
    @Override
    // reactive equivalent of void
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String ip = ipExtractor.extract(exchange.getRequest());
       

        //First, check if ratelimiter passes
        return Mono.fromCallable(() -> rateLimiter.isAllowed(ip))
                /*By default WebFlux runs everything on a small pool of non-blocking threads (Netty event loop threads).
                 These threads must never block. rateLimiter.isAllowed() calls Redis synchronously — that's a blocking call. */
               //boundedElastic is a separate thread pool designed for blocking work. 
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(allowed -> {
                    if (!allowed) {
                        return sendLimitResponse(exchange.getResponse());
                    }

                    if (isPublicPath(path)) {
                        return chain.filter(exchange);
                    }

                    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);

                        return authServiceClient.validateToken(new TokenValidationRequest(token))
                                .flatMap(validationData -> {
                                    if (validationData != null && validationData.isValid()) {
                                        List<SimpleGrantedAuthority> authorities = validationData.getUserInfo()
                                                .getRoles().stream()
                                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                                .toList();

                                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                                validationData.getUserInfo().getUserId(),
                                                null,
                                                authorities);
                                          /*ServerWebExchange is immutable in WebFlux — you can't modify the request headers directly. 
                                          .mutate() creates a builder copy of the exchange where you can make changes
                                          exchange.getRequest().getHeaders().add("X-User-Id", userId); is not possible
                                          
                                          */
                                        ServerWebExchange mutatedExchange = exchange.mutate()
                                                .request(r -> r
                                                        .header("X-User-Id", validationData.getUserInfo().getUserId())
                                                        .header("X-User-Roles",
                                                                String.join(",",
                                                                        validationData.getUserInfo().getRoles())))
                                                .build();
                                      // Pass mutatedExchange downstream so controllers see the new headers

                                      /*In a traditional servlet app, Spring Security stores the authenticated user in a ThreadLocal — one thread per request, so it's safe. In WebFlux, one request can hop across multiple threads, so ThreadLocal doesn't work.
Instead, WebFlux uses the reactive context — a key-value store that travels with the pipeline regardless of which thread is running. .contextWrite() writes the authentication into that context so that anything downstream (controllers, services) can read it via ReactiveSecurityContextHolder.getContext(). */
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
                });
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

    private Mono<Void> sendLimitResponse(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory()
                .wrap("{\"error\": \"Rate limit exceeded\"}".getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}