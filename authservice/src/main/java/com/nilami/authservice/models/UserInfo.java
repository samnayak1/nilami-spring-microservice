package com.nilami.authservice.models;


import java.math.BigDecimal;
import java.util.Date;
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
    private BigDecimal balance;
    private String address;
    private String bio;
    private Date created;
    
 
}
