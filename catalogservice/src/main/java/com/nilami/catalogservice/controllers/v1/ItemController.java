package com.nilami.catalogservice.controllers.v1;

import lombok.RequiredArgsConstructor;

import java.net.URL;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nilami.catalogservice.controllers.requestTypes.AddPicturesToItemRequest;
import com.nilami.catalogservice.controllers.requestTypes.CreateItemRequestType;
import com.nilami.catalogservice.dto.ApiResponse;
import com.nilami.catalogservice.dto.ItemDTO;
import com.nilami.catalogservice.models.Item;
import com.nilami.catalogservice.services.serviceAbstractions.FileUploadService;
import com.nilami.catalogservice.services.serviceAbstractions.ItemService;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    private final FileUploadService fileUploadService;

    @GetMapping("/test")
    public ResponseEntity<String> testController(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles) {

        System.out.println("roles: " + roles);
        System.out.println("userId: " + userId);
        return ResponseEntity.ok("Hello");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItem(@PathVariable String id) {
        try {
            return ResponseEntity.ok(itemService.getItem(id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve item: " + e.getMessage(), e);
        }
    }

    @GetMapping
    public ResponseEntity<Page<ItemDTO>> getAllItems(Pageable pageable, @RequestHeader("X-User-Id") String userId) {
        try {
            System.out.println("User: " + userId + " requested to get all items");
            return ResponseEntity.ok(itemService.getAllItems(pageable));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve items: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{id}/expiry")
    public ResponseEntity<Boolean> checkExpiry(@PathVariable String id) {
        try {
            return ResponseEntity.ok(itemService.checkIfExpiryDatePassed(id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to check expiry: " + e.getMessage(), e);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<String>> createItem(
            @RequestBody CreateItemRequestType request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Item response = itemService.createItem(request, userId);
            return new ResponseEntity<ApiResponse<String>>(
                    new ApiResponse<String>(true, "Item created", response.getId().toString()),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create item: " + e.getMessage(), e);
        }
    }




    
    @PutMapping("/pictures")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<List<URL>>> addPicturesToItem(
            @RequestBody AddPicturesToItemRequest request,
            @RequestHeader("X-User-Id") String userId) {
        try {

            Boolean hasPicturesBeenAdded = itemService.savePictureIdsForItem(request.getItemId(), userId,
                    request.getPictureIds());

            if (!hasPicturesBeenAdded) {
                return ResponseEntity
                        .internalServerError()
                        .body(new ApiResponse<List<URL>>(false, "Something wrong happened with adding pictures", null));
            }

            List<String> pictures = request.getPictureIds();

            List<URL> pictureUrls = pictures.stream()
                    .map(pictureId -> fileUploadService
                            .generateDownloadPresignedUrl(request.getItemId() + "/" + pictureId))
                    .toList();

            return ResponseEntity.ok().body(new ApiResponse<List<URL>>(true, "pictures have been added", pictureUrls));

        } catch (Exception e) {
            throw new RuntimeException("Failed to create item: " + e.getMessage(), e);
        }
    }
    @GetMapping("/search")
    public ResponseEntity<Page<ItemDTO>> searchItems(
            @RequestParam String keyword,
            Pageable pageable) {
        try {
            return ResponseEntity.ok(itemService.searchItem(keyword, pageable));
        } catch (Exception e) {
            throw new RuntimeException("Failed to search items: " + e.getMessage(), e);
        }
    }
}
