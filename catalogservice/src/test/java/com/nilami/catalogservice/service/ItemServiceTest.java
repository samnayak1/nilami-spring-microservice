package com.nilami.catalogservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.nilami.catalogservice.controllers.requestTypes.CreateItemRequestType;
import com.nilami.catalogservice.dto.ItemDTO;
import com.nilami.catalogservice.models.Category;
import com.nilami.catalogservice.models.Item;
import com.nilami.catalogservice.repositories.CategoryRepository;
import com.nilami.catalogservice.repositories.ItemRepository;
import com.nilami.catalogservice.services.serviceImplementations.ItemServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Category category;
    private Item item;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(UUID.randomUUID())
                .name("Electronics")
                .description("Electronic items")
                .build();

        item = Item.builder()
                .id(UUID.randomUUID())
                .title("Laptop")
                .description("Gaming Laptop")
                .basePrice(BigDecimal.valueOf(1500))
                .brand("Asus")
                .creatorUserId("user123")
                .pictureIds(List.of("pic1", "pic2"))
                .category(category)
                .expiryTime(new Date(System.currentTimeMillis() + 100000)) // future
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }

    @Test
    void testGetItem() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemDTO result = itemService.getItem(item.getId().toString());

        assertNotNull(result);
        assertEquals("Laptop", result.getTitle());
        verify(itemRepository, times(1)).findById(item.getId());
    }

    @Test
    void testGetAllItems() {
        Page<Item> page = new PageImpl<>(List.of(item));
        when(itemRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<ItemDTO> result = itemService.getAllItems(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getTitle());
        verify(itemRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void testCheckIfExpiryDatePassed_NotExpired() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        boolean result = itemService.checkIfExpiryDatePassed(item.getId().toString());

        assertFalse(result);
    }

    @Test
    void testCheckIfExpiryDatePassed_Expired() {
        item.setExpiryTime(new Date(System.currentTimeMillis() - 100000)); // past
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        boolean result = itemService.checkIfExpiryDatePassed(item.getId().toString());

        assertTrue(result);
    }

    @Test
    void testCreateItem() {
        CreateItemRequestType request = CreateItemRequestType.builder()
                .title("Phone")
                .description("Smartphone")
                .basePrice(BigDecimal.valueOf(800))
                .brand("Samsung")
    
              //  .pictureIds(List.of("pic3", "pic4"))
                .categoryId(category.getId().toString())
                .expiryTime(new Date(System.currentTimeMillis() + 200000))
                .build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item result = itemService.createItem(request,"user456");

        assertNotNull(result);
        assertEquals("Laptop", result.getTitle()); // saved mock returns "item" object
        verify(itemRepository, times(1)).save(any(Item.class));
    }

@Test
void testSearchItem() {
    Pageable pageable = PageRequest.of(0, 10);
    
    when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            "lap", "lap", pageable))
        .thenReturn(new PageImpl<>(List.of(item), pageable, 1));

    Page<ItemDTO> results = itemService.searchItem("lap", pageable);

    assertEquals(1, results.getNumberOfElements());
    assertEquals("Laptop", results.toList().get(0).getTitle());

    verify(itemRepository, times(1))
        .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("lap", "lap", pageable);
}

}
