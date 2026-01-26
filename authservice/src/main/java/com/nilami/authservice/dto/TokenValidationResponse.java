package com.nilami.authservice.dto;



import com.nilami.authservice.models.UserInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TokenValidationResponse {
    private boolean valid;
    private String message;
    private UserInfo userInfo;
}