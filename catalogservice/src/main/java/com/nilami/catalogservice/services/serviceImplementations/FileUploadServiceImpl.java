package com.nilami.catalogservice.services.serviceImplementations;

import java.net.URL;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nilami.catalogservice.configs.AwsS3Properties;
import com.nilami.catalogservice.services.serviceAbstractions.FileUploadService;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

     private final AwsS3Properties awsS3Properties;
     private S3Presigner presigner; 

 
    protected FileUploadServiceImpl(AwsS3Properties props,@Autowired(required = false) S3Presigner presigner) {
        this.awsS3Properties = props;
        this.presigner = presigner;
    }

    @PostConstruct
    public void init() {
        log.info("FileUploadService initialized successfully");
        log.info("AWS Region: {}", awsS3Properties.getRegion());
        log.info("AWS Bucket: {}", awsS3Properties.getBucketName());
           if (this.presigner == null) {
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                awsS3Properties.getAccessKey(),
                awsS3Properties.getSecretKey()
            );

            this.presigner = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.of(awsS3Properties.getRegion()))
                .build();
        }
    }
    

    public URL generatePresignedUrl(String objectName, String objectId) {
        try {

            String objectKey = objectId + "/" + objectName;
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(this.awsS3Properties.getBucketName())
                    .key(objectKey)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(b -> b

                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest));

            return presignedRequest.url();
        } catch (Exception e) {
            System.out.println("ERROR:" + e.getLocalizedMessage());
            throw e;
        }
    }

    public URL generateDownloadPresignedUrl(String objectKey) {
        try {
            // String contentType = getContentType(objectKey);
            // System.out.println("content type is"+contentType);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(this.awsS3Properties.getBucketName())
                    .key(objectKey)
                    // .responseContentType(contentType)
                    .build();

            PresignedGetObjectRequest presignedGetRequest = presigner.presignGetObject(b -> b
                    .signatureDuration(Duration.ofMinutes(10)) // Set an expiration time
                    .getObjectRequest(getObjectRequest));

            return presignedGetRequest.url();

        } catch (Exception e) {
            System.out.println("ERROR" + e.getLocalizedMessage());
            throw e;
        }

    }

  

    /*
     * private String getContentType(String filename) {
     * try {
     * Path filePath = Paths.get(filename);
     * return Files.probeContentType(filePath); // Automatically detect MIME type
     * based on file extension
     * } catch (IOException e) {
     * e.printStackTrace();
     * return "application/octet-stream"; // Default fallback
     * }
     * }
     */

}