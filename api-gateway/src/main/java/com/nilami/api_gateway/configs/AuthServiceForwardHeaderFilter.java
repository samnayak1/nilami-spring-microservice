package com.nilami.api_gateway.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;
import com.nilami.api_gateway.externalServices.UserClient;
import com.nilami.dto.TokenValidationRequest;
import com.nilami.dto.TokenValidationResponse;

import java.io.IOException;
import java.util.List;

@Component
public class AuthServiceForwardHeaderFilter extends OncePerRequestFilter {

    private final UserClient authServiceClient;

    public AuthServiceForwardHeaderFilter(UserClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

 @SuppressWarnings("null")

@Override
protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);

        try {
            TokenValidationRequest validationRequest = new TokenValidationRequest(token);
            TokenValidationResponse validationData = authServiceClient.validateToken(validationRequest);

            if (validationData != null && validationData.isValid() && validationData.getUserInfo() != null) {
                List<SimpleGrantedAuthority> authorities = validationData.getUserInfo().getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        validationData.getUserInfo().getUserId(),
                        null,
                        authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
                mutableRequest.putHeader("X-User-Id", validationData.getUserInfo().getUserId());
                mutableRequest.putHeader("X-User-Roles", String.join(",", validationData.getUserInfo().getRoles()));

                filterChain.doFilter(mutableRequest, response);
                return;
            }
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
           
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
    }

   
    filterChain.doFilter(request, response);
}
@SuppressWarnings("null")
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();

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