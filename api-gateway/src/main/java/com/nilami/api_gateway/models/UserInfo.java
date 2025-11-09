package com.nilami.api_gateway.models;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@RequiredArgsConstructor
public class UserInfo {
    private String userId;
    private String username;
    private String email;
    private List<String> roles;
    private Instant tokenExpiry;
}
