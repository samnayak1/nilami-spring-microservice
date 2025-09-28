package com.nilami.authservice.models;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@Setter
@Table(name = "users")
public class UserModel {

    @Id
    @UuidGenerator
    private UUID id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private int age;

    private String profilePicture;

    private String bio;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private BigDecimal balance = BigDecimal.TEN;


    private String address;

    @Enumerated(EnumType.STRING)
    private Roles role = Roles.CUSTOMER;  // Default role

    @CreationTimestamp
    @Column(updatable = false)
    private Date created;

    @UpdateTimestamp
    private Date updated;
}

