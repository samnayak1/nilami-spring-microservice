package com.nilami.bidservice.dto;

import java.math.BigDecimal;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class GetIdempotentKeyRequest {

    private String itemId;

    private BigDecimal bidAmount;

}
