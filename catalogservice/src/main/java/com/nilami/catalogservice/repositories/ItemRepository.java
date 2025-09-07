package com.nilami.catalogservice.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nilami.catalogservice.models.Item;

public interface ItemRepository extends JpaRepository<Item,UUID>{

    Page<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String keyword, String keyword2,
            Pageable pageable);
    
}
