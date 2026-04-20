package com.nilami.catalogservice.dto;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"pageable"})

/*pageImpl also serializes the pageable field which contains a PageRequest that Jackson can't deserialize. 
Fixed by telling Jackson to ignore the pageable field and reconstruct it from the primitive fields instead */
public class ListCacheablePage<T> extends PageImpl<T> {

    @JsonCreator
    public ListCacheablePage(
        @JsonProperty("content") List<T> content,
        @JsonProperty("number") int number,
        @JsonProperty("size") int size,
        @JsonProperty("totalElements") long totalElements
    ) {
        super(content, PageRequest.of(number, size), totalElements);
    }
}