package com.nilami.api_gateway.models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import com.nilami.api_gateway.controllers.requestTypes.Gender;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Getter
@RequiredArgsConstructor
@Setter
public class UserModel {
 
    private UUID id;

    private String name;

    private String email;

    private int age;

    private String profilePicture;

    private String bio;


    private Gender gender;

    private BigDecimal balance = BigDecimal.TEN;


    private String address;


    private Roles role; 

 
    private Date created;


    private Date updated; 
}
