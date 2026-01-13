package com.nilami.bidservice.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nilami.bidservice.models.IdempotentKeys;

public interface IdempotentKeyRepository extends JpaRepository<IdempotentKeys,UUID> {

    
}
