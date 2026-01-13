package com.nilami.catalogservice.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nilami.catalogservice.dto.SimplifiedItemDTO;
import com.nilami.catalogservice.models.Item;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    Page<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String keyword, String keyword2,
            Pageable pageable);


     //Unset splits it up like VALUES (1), (2), (3), (4)). This is called a virtual table
    @Query(value = """
            SELECT 
            i.id,
            i.title,
            i.base_price as basePrice,
            i.brand,
            i.expiry_time as expiryTime,
            i.deleted
            FROM items i
            JOIN UNNEST(:ids) AS virtual_list(id)
            ON i.id = virtual_list.id
            """, nativeQuery = true)
    List<SimplifiedItemDTO> findItemsByVirtualIdList(UUID[] ids);


    Page<Item> findByCategoryId(String categoryId, Pageable pageable);

}
