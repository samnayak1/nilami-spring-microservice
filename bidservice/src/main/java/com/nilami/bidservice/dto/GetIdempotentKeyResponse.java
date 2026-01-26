package com.nilami.bidservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class GetIdempotentKeyResponse {

    private String idempotentKey;

    private UUID itemId;

    private BigDecimal bidAmount;

    private Instant created;

}
