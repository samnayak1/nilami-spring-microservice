package com.nilami.authservice.controllers.requestTypes;

import lombok.Getter;

@Getter

public class LoginRequest {
      
     private String email;

     private String password;
}
