package com.nilami.bidservice.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.nilami.bidservice.models.SagaLogs;
import com.nilami.bidservice.models.SagaState;

import jakarta.transaction.Transactional;

public interface SagaLogsRepository extends JpaRepository<SagaLogs,UUID>{
    
    @Modifying
    @Transactional
    @Query("UPDATE SagaLogs s SET s.currentState = :newState, s.updatedAt = CURRENT_TIMESTAMP WHERE s.sagaId = :id")
    void updateStatus(UUID id, SagaState newState);
}
