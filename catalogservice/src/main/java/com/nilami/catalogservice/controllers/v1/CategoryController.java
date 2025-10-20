package com.nilami.catalogservice.controllers.v1;




import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nilami.catalogservice.controllers.requestTypes.CategoryRequest;
import com.nilami.catalogservice.dto.CategoryDTO;
import com.nilami.catalogservice.services.serviceImplementations.CategoryServiceImpl;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryServiceImpl categoryService;

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable String id) {
        try {
            return ResponseEntity.ok(categoryService.getCategory(id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve category: " + e.getMessage(), e);
        }
    }

    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(Pageable pageable) {
        try {
            return ResponseEntity.ok(categoryService.getAllCategories(pageable));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve categories: " + e.getMessage(), e);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryRequest request) {
        try {
            CategoryDTO response = categoryService.createCategory(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create category: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable String id,
            @RequestBody CategoryRequest request) {
        try {
            return ResponseEntity.ok(categoryService.updateCategory(id, request));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update category: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete category: " + e.getMessage(), e);
        }
    }
}
