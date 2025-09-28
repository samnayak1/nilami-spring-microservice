package com.nilami.authservice.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nilami.authservice.models.UserModel;

@Repository
public interface UserRepository extends JpaRepository<UserModel,UUID> {

    boolean existsByEmail(String email);
    
}
