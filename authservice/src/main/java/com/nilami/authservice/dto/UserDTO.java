package com.nilami.authservice.dto;

import java.math.BigDecimal;

import java.util.UUID;

import com.nilami.authservice.models.Roles;
import com.nilami.authservice.models.UserModel;

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

      
    public static UserDTO toUserDTO(UserModel user) {
           return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .email(user.getEmail())
                .age(user.getAge())
                .role(user.getRole())
                .balance(user.getBalance())
                .bio(user.getBio())
                .build();
    }

}

