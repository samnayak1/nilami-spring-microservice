package com.nilami.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import com.nilami.catalogservice.models.Category;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private String id;

    private String name;

    private String description;




    public static CategoryDTO toCategoryDTO(Category category) {
  

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
           
                .build();
    }
}
