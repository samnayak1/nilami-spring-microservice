package com.nilami.catalogservice.services.serviceImplementations;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nilami.catalogservice.controllers.requestTypes.CategoryRequest;
import com.nilami.catalogservice.dto.CategoryDTO;
import com.nilami.catalogservice.models.Category;
import com.nilami.catalogservice.repositories.CategoryRepository;
import com.nilami.catalogservice.services.serviceAbstractions.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
       private final CategoryRepository categoryRepository;

    @Override
    public CategoryDTO getCategory(String categoryId) {
        UUID categoryIdInUUID=UUID.fromString(categoryId);
        Category category = categoryRepository.findById(categoryIdInUUID)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryDTO.toCategoryDTO(category);
    }

    @Override
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        Page<Category> page = categoryRepository.findAll(pageable);
        List<CategoryDTO> dtoList = page.getContent().stream()
                .map(CategoryDTO::toCategoryDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Override
    public CategoryDTO createCategory(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        Category saved = categoryRepository.save(category);
        System.out.println("saved:"+saved);
        return CategoryDTO.toCategoryDTO(saved);
    }

    @Override
    public CategoryDTO updateCategory(String categoryId, CategoryRequest request) {
         UUID categoryIdInUUID=UUID.fromString(categoryId);
        Category category = categoryRepository.findById(categoryIdInUUID)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if(request.getName()!=null){
          category.setName(request.getName());
        }
        if(request.getDescription()!=null){
         category.setDescription(request.getDescription());
        }
        Category updated = categoryRepository.save(category);
        return CategoryDTO.toCategoryDTO(updated);
    }

    @Override
    public void deleteCategory(String categoryId) {
         UUID categoryIdInUUID=UUID.fromString(categoryId);
        categoryRepository.deleteById(categoryIdInUUID);
    }
}
