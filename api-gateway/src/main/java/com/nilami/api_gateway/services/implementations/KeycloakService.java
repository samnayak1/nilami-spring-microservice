package com.nilami.api_gateway.services.implementations;



import java.util.List;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nilami.api_gateway.controllers.requestTypes.SignupRequest;
import com.nilami.api_gateway.exceptions.KeycloakClientError;
import com.nilami.api_gateway.services.UserAuthSignupService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

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
        user.setUsername(signupRequest.getEmail());
        user.setEmail(signupRequest.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setFirstName(signupRequest.getName());
        user.setLastName("-");

        UsersResource usersResource = keycloak.realm(realm).users();
        Response response = usersResource.create(user);

        if (response.getStatus() != 201) {
            String errorMsg = "Failed to create user in Keycloak. Status: " + response.getStatus();
            response.close();
            throw new KeycloakClientError(errorMsg);
        }


        String userId = CreatedResponseUtil.getCreatedId(response);
        response.close();

     
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(signupRequest.getPassword());

        keycloak.realm(realm)
                .users()
                .get(userId)
                .resetPassword(passwordCred);

  
        RoleRepresentation customerRole = keycloak.realm(realm)
                .roles()
                .get("CUSTOMER")
                .toRepresentation();

        keycloak.realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(customerRole));

    
        return userId;

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

    public String getUserId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject();
    }

    public String getEmail(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaimAsString("email");
    }

}
