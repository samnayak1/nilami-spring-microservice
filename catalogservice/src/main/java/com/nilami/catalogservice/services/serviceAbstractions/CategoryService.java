package com.nilami.catalogservice.services.serviceAbstractions;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nilami.catalogservice.controllers.requestTypes.CategoryRequest;
import com.nilami.dto.CategoryDTO;

public interface CategoryService {
   CategoryDTO getCategory(String categoryId);

    Page<CategoryDTO> getAllCategories(Pageable pageable);

    CategoryDTO createCategory(CategoryRequest request);

    CategoryDTO updateCategory(String categoryId, CategoryRequest request);

    void deleteCategory(String categoryId);   
}
