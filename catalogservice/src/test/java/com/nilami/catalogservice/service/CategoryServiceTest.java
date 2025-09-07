package com.nilami.catalogservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;


import com.nilami.catalogservice.controllers.requestTypes.CategoryRequest;
import com.nilami.catalogservice.models.Category;
import com.nilami.catalogservice.repositories.CategoryRepository;
import com.nilami.catalogservice.services.serviceImplementations.CategoryServiceImpl;
import com.nilami.dto.CategoryDTO;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

      @Test
    void createCategory_ShouldSaveAndReturnDto() {
        // Given
        CategoryRequest request = new CategoryRequest("Electronics", "All gadgets");
        UUID categoryId = UUID.randomUUID();
        Category savedCategory = new Category(categoryId, "Electronics", "All gadgets", null);
        
        // When - mock the save method
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        
        // Then - call the service method
        CategoryDTO response = categoryService.createCategory(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("Electronics", response.getName());
        assertEquals("All gadgets", response.getDescription());
        assertEquals(categoryId, response.getId());
        
        // Verify the interaction
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void getCategoryById_ShouldReturnCategory_WhenExists() {
        UUID id = UUID.randomUUID();
        Category savedCategory = new Category(id, "Fashion", "Clothes", null);

        when(categoryRepository.findById(id)).thenReturn(Optional.of(savedCategory));
        
        CategoryDTO response = categoryService.getCategory(id.toString());

        assertTrue((response.getName()).equals("Fashion"));
    }
}
