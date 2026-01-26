package com.nilami.authservice.services;

import com.nilami.authservice.controllers.requestTypes.SignupRequest;

public interface UserAuthSignupService {

    void deleteUser(String keycloakUserId);

    String createUser(SignupRequest signupRequest);
    
}
