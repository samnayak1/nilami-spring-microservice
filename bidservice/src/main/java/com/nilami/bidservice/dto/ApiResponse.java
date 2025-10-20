package com.nilami.bidservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApiResponse<T> {
    private Boolean success;
    private String message;
    private T data;
}