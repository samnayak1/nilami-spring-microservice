package com.nilami.catalogservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nilami.catalogservice.models.Category;

public interface CategoryRepository extends JpaRepository<Category,String>{
    
}
