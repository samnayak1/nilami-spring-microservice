package com.nilami.catalogservice.controllers.v1;

import java.net.URL;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.nilami.catalogservice.controllers.requestTypes.GetPresignedUrlRequest;
import com.nilami.catalogservice.controllers.requestTypes.MessageResponse;
import com.nilami.catalogservice.dto.ApiResponse;
import com.nilami.catalogservice.dto.ItemDTO;
import com.nilami.catalogservice.services.serviceAbstractions.FileUploadService;
import com.nilami.catalogservice.services.serviceAbstractions.ItemService;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/v1/items/file")
@RequiredArgsConstructor
public class FileUploadController {

   
    private final FileUploadService fileUploadService;

    private final ItemService itemService;

    @PutMapping("/presigned-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generatePresignedUrlForItem(
            @RequestBody GetPresignedUrlRequest requestBody,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles ) {
        try {
            System.out.println("request body " + requestBody.toString());
            ItemDTO item=itemService.getItem(requestBody.getObjectId());
            if(item==null){
                return ResponseEntity.badRequest().body(new ApiResponse<>(false,"item not found",null));
            }
            if(!item.getCreatorUserId().equals(userId)){
                return ResponseEntity.badRequest().body(new ApiResponse<>(false,"not creator of the item",null));
            }

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