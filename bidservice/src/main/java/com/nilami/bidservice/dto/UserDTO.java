package com.nilami.bidservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO {
    private UUID id;

    private String name;

    private String email;

    private int age;

    private String profilePicture;

    private String bio;

    private Roles role;

    private BigDecimal balance;

      


}