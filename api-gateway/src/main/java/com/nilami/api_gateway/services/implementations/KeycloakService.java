package com.nilami.api_gateway.services.implementations;

import java.util.ArrayList;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import com.nilami.api_gateway.controllers.requestTypes.SignupRequest;
import com.nilami.api_gateway.exceptions.KeycloakClientError;
import com.nilami.api_gateway.services.UserAuthSignupService;

import jakarta.ws.rs.core.Response;

@Service
public class KeycloakService implements UserAuthSignupService {

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

 
    public String createUser(SignupRequest signupRequest) {
        try {
            UserRepresentation user = new UserRepresentation();
            user.setUsername(signupRequest.getName());
            user.setEmail(signupRequest.getEmail());
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Create credentials
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(signupRequest.getPassword());
            //The password is permanent. The user can continue logging in with it until it’s manually changed
            credential.setTemporary(false);

            // Attach credentials
            List<CredentialRepresentation> credentials = new ArrayList<>();
            credentials.add(credential);
            user.setCredentials(credentials);

            // Create user
            UsersResource usersResource = keycloak.realm(realm).users();
            Response response = usersResource.create(user);

            if (response.getStatus() == 201) {
                String locationHeader = response.getHeaderString("Location");
                String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
                response.close();
                return userId;
            } else {
                throw new KeycloakClientError("Failed to create user in Keycloak. Status: " + response.getStatus());
            }

        } catch (Exception e) {
            throw new KeycloakClientError("Error creating user in Keycloak", e);
        }
    }

    public void deleteUser(String userId) {
        try {
            UsersResource usersResource = keycloak.realm(realm).users();
            Response response = usersResource.delete(userId);

            if (response.getStatus() != 204) {
                throw new KeycloakClientError("Failed to delete user from Keycloak. Status: " + response.getStatus());
            }
            response.close();

        } catch (Exception e) {
            throw new KeycloakClientError("Error deleting user from Keycloak", e);
        }
    }

}
