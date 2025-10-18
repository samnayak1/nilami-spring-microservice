package com.nilami.catalogservice.controllers.v1;




import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ItemDTO> getItem
                               (@PathVariable String id) {

        return ResponseEntity.ok(itemService.getItem(id));
    }

    @GetMapping
    public ResponseEntity<Page<ItemDTO>> getAllItems(Pageable pageable) {
        return ResponseEntity.ok(itemService.getAllItems(pageable));
    }

    @GetMapping("/{id}/expiry")
    public ResponseEntity<Boolean> checkExpiry(@PathVariable String id) {
        return ResponseEntity.ok(itemService.checkIfExpiryDatePassed(id));
    }

    @PostMapping
    public ResponseEntity<ItemDTO> createItem(
        @RequestBody CreateItemRequestType request,
         @RequestHeader("X-User-Id") String userId) {
        
        ItemDTO response = itemService.createItem(request,userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ItemDTO>> searchItems(
            @RequestParam String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(itemService.searchItem(keyword, pageable));
    }
}

