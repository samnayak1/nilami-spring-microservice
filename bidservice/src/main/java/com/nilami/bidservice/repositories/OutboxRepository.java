package com.nilami.bidservice.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.nilami.bidservice.dto.OutboxStatus;
import com.nilami.bidservice.models.OutboxEvent;

import jakarta.persistence.LockModeType;


public interface OutboxRepository extends JpaRepository<OutboxEvent,UUID>{

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxEvent> findTop50ByStatusOrderByCreatedAt(OutboxStatus status);
    
}
