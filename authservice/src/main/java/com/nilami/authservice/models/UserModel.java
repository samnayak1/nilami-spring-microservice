package com.nilami.authservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Entity
@Getter
@Setter
@Table(name = "users")
public class UserModel {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Version
    private Long version;

    @Column
    private int age;
    @Column
    private String profilePicture;
    @Column
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.TEN;
    //keep it 10 dollars as default for now so they can spend money
    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles role = Roles.CUSTOMER; // Default role

    @Column(name = "reserved_balance", nullable = false, precision = 19, scale = 2)
    
    private BigDecimal reservedBalance = BigDecimal.ZERO;

    public BigDecimal getAvailableBalance() {
        return balance.subtract(reservedBalance);
    }

    @CreationTimestamp
    @Column(updatable = false)
    private Date created;

    @UpdateTimestamp
    @Column(updatable = false)
    private Date updated;


}
