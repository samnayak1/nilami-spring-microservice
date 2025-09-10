package com.nilami.authservice.models;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> roles;

    @Column
    @Builder.Default
    private boolean active = true;
}

