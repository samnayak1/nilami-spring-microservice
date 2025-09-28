package com.nilami.authservice.services;

import com.nilami.authservice.controllers.requestTypes.SignupRequest;

public interface UserSignupService {
    public String signupUser(SignupRequest request);
}
