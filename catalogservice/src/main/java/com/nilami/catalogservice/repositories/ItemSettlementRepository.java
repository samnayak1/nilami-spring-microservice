package com.nilami.catalogservice.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nilami.catalogservice.models.ItemSettlement;

public interface ItemSettlementRepository extends JpaRepository<ItemSettlement, UUID> {
    
}
