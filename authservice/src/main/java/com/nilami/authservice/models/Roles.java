package com.nilami.authservice.models;

import java.util.Set;




public enum Roles {
    CUSTOMER(Set.of("ROLE_CUSTOMER")),
    SELLER(Set.of("ROLE_SELLER")),
    ADMIN(Set.of("ROLE_ADMIN"));

    private final Set<String> authorities;


    Roles(Set<String> authorities) {
        this.authorities = authorities;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }
}