package com.nilami.bidservice.dto;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Roles {
   CUSTOMER(Set.of("ROLE_CUSTOMER")),
    SELLER(Set.of("ROLE_SELLER")),
    ADMIN(Set.of("ROLE_ADMIN"));

    private final Set<String> authorities;

    Roles(Set<String> authorities) {
        this.authorities = authorities;
    }

    public Set<SimpleGrantedAuthority> getAuthorities() {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}