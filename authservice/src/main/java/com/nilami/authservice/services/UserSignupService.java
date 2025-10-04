package com.nilami.authservice.services;

import com.nilami.authservice.controllers.requestTypes.SignupRequest;
import com.nilami.authservice.models.UserModel;

public interface UserSignupService {
    public UserModel signupUser(SignupRequest request);
}
