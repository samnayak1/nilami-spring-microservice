package com.nilami.authservice.repositories;


import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nilami.authservice.models.BalanceReservation;
import com.nilami.authservice.models.ReservationStatus;

@Repository
public interface BalanceReservationRepository extends JpaRepository<BalanceReservation, UUID> {
    
    Optional<BalanceReservation> findByIdempotentKey(String idempotentKey);
    
    List<BalanceReservation> findByUserIdAndStatus(UUID userId, ReservationStatus status);
    
    List<BalanceReservation> findByStatusAndExpiresAtBefore(
        ReservationStatus status, 
        Date dateTime
    );
}