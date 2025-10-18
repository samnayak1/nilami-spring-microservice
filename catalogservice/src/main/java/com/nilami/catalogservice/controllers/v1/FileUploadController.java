package com.nilami.catalogservice.controllers.v1;

import java.net.URL;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nilami.catalogservice.controllers.requestTypes.GetPresignedUrlRequest;
import com.nilami.catalogservice.controllers.requestTypes.MessageResponse;
import com.nilami.catalogservice.services.serviceAbstractions.FileUploadService;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileUploadController {

   
    private FileUploadService fileUploadService;

    @PutMapping("/presigned-url")
    public ResponseEntity<?> generatePresignedUrl(
            @RequestBody GetPresignedUrlRequest requestBody) {
        try {
            System.out.println("request body " + requestBody.toString());
            URL presignedUrl = fileUploadService.generatePresignedUrl(
                    requestBody.getFileName(),
                    requestBody.getObjectId());
            return ResponseEntity.ok(new MessageResponse(presignedUrl.toString()));

        } catch (Exception e) {
            System.out.println("error " + e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}