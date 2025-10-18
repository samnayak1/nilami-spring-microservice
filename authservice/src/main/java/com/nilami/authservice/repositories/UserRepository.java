package com.nilami.authservice.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nilami.authservice.models.UserModel;

import jakarta.persistence.LockModeType;

@Repository
public interface UserRepository extends JpaRepository<UserModel,UUID> {

    boolean existsByEmail(String email);


    //Row lock so it does not break when same user bids for more than one or more items at the same time.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT u FROM UserModel u WHERE u.id = :userId")
Optional<UserModel> findByIdWithLock(UUID userId);
    
}
