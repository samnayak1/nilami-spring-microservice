package com.nilami.bidservice.models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Bid {
  @Id
  @UuidGenerator
  private UUID id;

  @CreationTimestamp
  private Date created;

  @UpdateTimestamp
  private Date updated;


  @Column
  private String itemId;

  @Column
  private String creatorId;

  @Column
  private BigDecimal price;



}