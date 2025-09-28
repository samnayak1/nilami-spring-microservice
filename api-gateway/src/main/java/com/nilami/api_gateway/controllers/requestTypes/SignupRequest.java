package com.nilami.api_gateway.controllers.requestTypes;


import lombok.Getter;

@Getter
public class SignupRequest {

    private String name;
    private String email;
    private String password;
    private int age;
    private Gender gender;
    private String address;

}
