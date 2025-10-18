package com.nilami.catalogservice.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Properties {
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucketName;
}