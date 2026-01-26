// package com.nilami.bidservice.models;

// import java.time.Instant;
// import java.util.UUID;

// import org.hibernate.annotations.CreationTimestamp;
// import org.hibernate.annotations.JdbcTypeCode;
// import org.hibernate.annotations.UuidGenerator;
// import org.hibernate.type.SqlTypes;

// import com.nilami.bidservice.dto.OutboxEventType;
// import com.nilami.bidservice.dto.OutboxStatus;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;
// import lombok.Builder;
// import lombok.EqualsAndHashCode;
// import lombok.Getter;
// import lombok.Setter;

// @Entity
// @Table(name = "outbox_event")
// @Getter
// @Setter
// @EqualsAndHashCode
// @Builder
// public class OutboxEvent {

//     @Id
//     @UuidGenerator
//     private UUID id;

//     private String aggregateType;
//     private UUID aggregateId;

//     @Enumerated(EnumType.STRING)
//     private OutboxEventType eventType;

    
//     @JdbcTypeCode(SqlTypes.JSON)
//     @Column(columnDefinition = "jsonb")
//     private String payload;

//     @Enumerated(EnumType.STRING)
//     private OutboxStatus status;  //NEW  SENT or  FAILED

//     @CreationTimestamp
//     private Instant createdAt;
// }