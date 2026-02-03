package com.nilami.authservice.services.implementations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nilami.authservice.controllers.requestTypes.SignupRequest;
import com.nilami.authservice.services.UserAuthSignupService;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.MessageActionType;


@Service
public class CognitoService implements UserAuthSignupService {

    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    public CognitoService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    @Override
    public String createUser(SignupRequest signupRequest) {
        try {
    
            AdminCreateUserRequest userRequest = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(signupRequest.getEmail())
                    .userAttributes(
                            AttributeType.builder().name("email").value(signupRequest.getEmail()).build(),
                            AttributeType.builder().name("name").value(signupRequest.getName()).build(),
                            AttributeType.builder().name("email_verified").value("true").build()
                    )
                    .messageAction(MessageActionType.SUPPRESS) //No welcome mail

                    
                    .build();

            AdminCreateUserResponse response = cognitoClient.adminCreateUser(userRequest);
            String cognitoUserId = response.user().username(); 


            AdminSetUserPasswordRequest passwordRequest = AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(cognitoUserId)
                    .password(signupRequest.getPassword())
                    .permanent(true) 
                    .build();
            cognitoClient.adminSetUserPassword(passwordRequest);

       
            AdminAddUserToGroupRequest groupRequest = AdminAddUserToGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .username(cognitoUserId)
                    .groupName("CUSTOMER") 
                    .build();
            cognitoClient.adminAddUserToGroup(groupRequest);

            return cognitoUserId;

        } catch (CognitoIdentityProviderException e) {
            throw new RuntimeException("Error creating user in Cognito: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public void deleteUser(String userId) {
        try {
            AdminDeleteUserRequest deleteRequest = AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(userId)
                    .build();
            cognitoClient.adminDeleteUser(deleteRequest);
        } catch (CognitoIdentityProviderException e) {
            throw new RuntimeException("Error deleting user from Cognito", e);
        }
    }

    
}