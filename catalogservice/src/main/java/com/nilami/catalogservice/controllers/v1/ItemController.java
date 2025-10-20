package com.nilami.catalogservice.controllers.v1;




import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nilami.catalogservice.controllers.requestTypes.CreateItemRequestType;
import com.nilami.catalogservice.dto.ItemDTO;
import com.nilami.catalogservice.services.serviceAbstractions.ItemService;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItem(@PathVariable String id) {
        try {
            return ResponseEntity.ok(itemService.getItem(id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve item: " + e.getMessage(), e);
        }
    }

    @GetMapping
    public ResponseEntity<Page<ItemDTO>> getAllItems(Pageable pageable) {
        try {
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
    public ResponseEntity<ItemDTO> createItem(
            @RequestBody CreateItemRequestType request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            ItemDTO response = itemService.createItem(request, userId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
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
