package com.nilami.authservice.controllers.requestTypes;

import com.nilami.authservice.models.Gender;


import lombok.Getter;


          
@Getter
public class SignupRequest {
    private String name;
    private String email;
    private int age;
    private Gender gender;
    private String address;
    private String id;
}

