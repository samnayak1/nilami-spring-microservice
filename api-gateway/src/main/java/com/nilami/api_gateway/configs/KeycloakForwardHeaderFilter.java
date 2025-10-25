package com.nilami.api_gateway.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class KeycloakForwardHeaderFilter extends OncePerRequestFilter {

  
    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String userId = jwt.getSubject();

        
            @SuppressWarnings("unchecked")
            List<String> rolesList = Optional.ofNullable((Map<String, Object>) jwt.getClaim("realm_access"))
                    .map(realmAccess -> (List<String>) realmAccess.get("roles"))
                    .orElse(Collections.emptyList());

            String roles = String.join(",", rolesList);

      
            MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
            mutableRequest.putHeader("X-User-Id", userId);
            mutableRequest.putHeader("X-User-Roles", roles);
            System.out.println("userId: "+userId);
            System.out.println("roles: "+roles);
            filterChain.doFilter(mutableRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}