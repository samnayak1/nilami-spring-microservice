package com.nilami.bidservice.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.nilami.bidservice.models.Bid;

import jakarta.persistence.LockModeType;


@Repository
public interface BidRepository extends JpaRepository<Bid,UUID>{

    Optional<Bid> findTopByOrderByCreatedDesc();

     @Lock(LockModeType.PESSIMISTIC_READ)
     @NonNull
     Optional<Bid> findById(@NonNull UUID id);


}
