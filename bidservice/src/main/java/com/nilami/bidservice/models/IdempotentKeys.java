package com.nilami.bidservice.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Table;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "idempotent_keys")
public class IdempotentKeys {

  // the id will be our idempotent key
  @Id
  @UuidGenerator
  private UUID id;

  @Column(nullable = false)
  private BigDecimal bidAmount;

  @Column(nullable = false)
  private UUID creatorId;

  @Column(nullable = false)
  private UUID itemId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant created;

  @Column(nullable = false)
  private BidStatus bidStatus;

}
