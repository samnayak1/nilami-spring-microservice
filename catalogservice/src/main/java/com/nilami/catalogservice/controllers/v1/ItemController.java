package com.nilami.catalogservice.controllers.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import com.nilami.catalogservice.dto.SimplifiedItemDTO;
import com.nilami.catalogservice.models.Item;
import com.nilami.catalogservice.services.serviceAbstractions.FileUploadService;
import com.nilami.catalogservice.services.serviceAbstractions.ItemService;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/items/v1")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    private final FileUploadService fileUploadService;

    @GetMapping("/test")
    public ResponseEntity<String> testController(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId,
           @Parameter(hidden = true) @RequestHeader("X-User-Roles") String roles) {

        System.out.println("roles: " + roles);
        System.out.println("userId: " + userId);
        return ResponseEntity.ok("Hello");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItem(@PathVariable String id) {

            return ResponseEntity.ok(itemService.getItem(id));
      
    }

    @GetMapping
    public ResponseEntity<Page<ItemDTO>> getAllItems(Pageable pageable,
         @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId,
        @RequestParam(required = false) String categoryId) {
   
            log.debug("User: " + userId + " requested to get all items");
            return ResponseEntity.ok(itemService.getAllItems(categoryId, pageable));
    
    }

    @GetMapping("/{id}/expiry")
    public ResponseEntity<Boolean> checkExpiry(@PathVariable String id) {
   
            return ResponseEntity.ok(itemService.checkIfExpiryDatePassed(id));
     
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<String>> createItem(
            @RequestBody CreateItemRequestType request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {
     
            Item response = itemService.createItem(request, userId);
            return new ResponseEntity<ApiResponse<String>>(
                    new ApiResponse<String>(true, "Item created", response.getId().toString()),
                    HttpStatus.CREATED);
      
    }




    
    @PutMapping("/pictures")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<List<URL>>> addPicturesToItem(
            @RequestBody AddPicturesToItemRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {
  

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
                            .generateDownloadPresignedUrl(request.getItemId() + "/" + pictureId,60))
                    .toList();

            return ResponseEntity.ok().body(new ApiResponse<List<URL>>(true, "pictures have been added", pictureUrls));

    
    }
    @GetMapping("/search")
    public ResponseEntity<Page<ItemDTO>> searchItems(
            @RequestParam String keyword,
            Pageable pageable) {
  
            return ResponseEntity.ok(itemService.searchItem(keyword, pageable));
      
    }


    
    @PostMapping("/details")
    public ResponseEntity<List<SimplifiedItemDTO>> getItemDetails(
            @RequestBody List<String> itemIds
    ) {
 
        List<SimplifiedItemDTO> items =
                itemService.getItemDetailsGivenIds(itemIds);

        return ResponseEntity.ok(items);
      
    }



}
