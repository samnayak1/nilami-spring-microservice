package com.nilami.api_gateway.services;

import com.nilami.api_gateway.controllers.requestTypes.SignupRequest;

public interface UserAuthSignupService {

    void deleteUser(String keycloakUserId);

    String createUser(SignupRequest signupRequest);
    
}
