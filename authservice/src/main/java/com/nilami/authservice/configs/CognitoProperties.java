package com.nilami.authservice.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "aws.cognito")
@Getter
@Setter
@EqualsAndHashCode

public class CognitoProperties {
    private String userPoolId;
    private String clientId;
    private String clientSecret;
    private String region;
}