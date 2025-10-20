package com.nilami.bidservice.models;

import java.math.BigDecimal;
import java.time.Instant;

import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "bids")
public class Bid {
  @Id
  @UuidGenerator
  private UUID id;

  @CreationTimestamp
  private Instant created;

  @UpdateTimestamp
  private Instant updated;

  @Column(nullable = false)
  private String itemId;

  @Column(nullable = false)
  private String creatorId;

  @Column(nullable = false)
  private BigDecimal price;

}