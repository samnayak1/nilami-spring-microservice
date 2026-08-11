package com.nilami.catalogservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

import java.net.URI;

import java.time.Instant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.nilami.catalogservice.dto.ApiResponse;
import com.nilami.catalogservice.dto.GetHighestBidsRequest;
import com.nilami.catalogservice.dto.ItemDTO;
import com.nilami.catalogservice.models.Category;
import com.nilami.catalogservice.models.Item;
import com.nilami.catalogservice.repositories.CategoryRepository;
import com.nilami.catalogservice.repositories.ItemRepository;
import com.nilami.catalogservice.services.externalClients.BidClient;
import com.nilami.catalogservice.services.serviceAbstractions.FileUploadService;
import com.nilami.catalogservice.services.serviceImplementations.ItemCacheService;
import com.nilami.catalogservice.services.serviceImplementations.ItemServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;


    @Mock
    private BidClient bidClient;

    @Mock 
    private ItemCacheService itemCacheService;


    @InjectMocks
    private ItemServiceImpl itemService;



    @Mock
    private FileUploadService fileService;

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
                .pictureIds(List.of("pic1.png", "pic2.jpg"))
                .category(category)
                .expiryTime(new Date(System.currentTimeMillis() + 100000)) // future
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }
@Test
void testGetItem() throws Exception {
    //mock file service FIRST
    when(fileService.generateDownloadPresignedUrl(item.getId() + "/pic1.png", 60))
            .thenReturn(URI.create("https://mock-s3.com/pic1.png").toURL());
    when(fileService.generateDownloadPresignedUrl(item.getId() + "/pic2.jpg", 60))
            .thenReturn(URI.create("https://mock-s3.com/pic2.jpg").toURL());

    //mock cache service
    ItemDTO itemDTO = ItemDTO.toItemDTO(item, fileService);
    when(itemCacheService.getItemFacade(item.getId().toString())).thenReturn(itemDTO);

    Map<String, BigDecimal> highestBidsMap = new HashMap<>();
    highestBidsMap.put(item.getId().toString(), new BigDecimal("100.00"));
    
    ApiResponse<Map<String, BigDecimal>> mockResponse = 
        new ApiResponse<>(true, "Success", highestBidsMap);
    
    when(bidClient.getHighestBidsForItems(any(GetHighestBidsRequest.class)))
        .thenReturn(mockResponse);

    //execute
    ItemDTO result = itemService.getItem(item.getId().toString());

    //verify
    assertNotNull(result);
    assertEquals("Laptop", result.getTitle());
    assertEquals(2, result.getPictureIds().size());
    assertEquals(new BigDecimal("100.00"), result.getHighestBidPrice());
    
    verify(itemCacheService, times(1)).getItemFacade(item.getId().toString());
    verify(fileService, times(2)).generateDownloadPresignedUrl(anyString(), anyLong());
    verify(bidClient, times(1)).getHighestBidsForItems(any(GetHighestBidsRequest.class));
}

@Test
void testGetAllItems() throws Exception {
    //mock file service 
    when(fileService.generateDownloadPresignedUrl(item.getId() + "/pic1.png", 60))
            .thenReturn(URI.create("https://mock-s3.com/pic1.png").toURL());
    when(fileService.generateDownloadPresignedUrl(item.getId() + "/pic2.jpg", 60))
            .thenReturn(URI.create("https://mock-s3.com/pic2.jpg").toURL());

        
    ItemDTO itemDTO = ItemDTO.toItemDTO(item, fileService);
    
    Page<ItemDTO> cachedPage = new PageImpl<>(List.of(itemDTO), PageRequest.of(0, 10), 1);
    
    // mock cache servicew
    when(itemCacheService.getItemsListFacade(null, PageRequest.of(0, 10)))
        .thenReturn(cachedPage);

    //mock highest bid client service
    Map<String, BigDecimal> highestBidsMap = new HashMap<>();
    highestBidsMap.put(item.getId().toString(), new BigDecimal("100.00"));
    
    ApiResponse<Map<String, BigDecimal>> mockResponse = 
        new ApiResponse<>(true, "Success", highestBidsMap);

    when(bidClient.getHighestBidsForItems(any(GetHighestBidsRequest.class)))
        .thenReturn(mockResponse);

    // Execute
    Page<ItemDTO> result = itemService.getAllItems(null, PageRequest.of(0, 10));

    // Verify
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("Laptop", result.getContent().get(0).getTitle());
    assertEquals(new BigDecimal("100.00"), result.getContent().get(0).getHighestBidPrice());
    
    verify(itemCacheService, times(1)).getItemsListFacade(null, PageRequest.of(0, 10));
    verify(bidClient, times(1)).getHighestBidsForItems(any(GetHighestBidsRequest.class));
    verify(fileService, times(2)).generateDownloadPresignedUrl(anyString(), anyLong());
}

@Test
void testGetAllItemsWithCategoryId() throws Exception {
    String categoryId = UUID.randomUUID().toString();
    
    // mock file service
    when(fileService.generateDownloadPresignedUrl(item.getId() + "/pic1.png", 60))
            .thenReturn(URI.create("https://mock-s3.com/pic1.png").toURL());
    when(fileService.generateDownloadPresignedUrl(item.getId() + "/pic2.jpg", 60))
            .thenReturn(URI.create("https://mock-s3.com/pic2.jpg").toURL());

    // mock cache service
    ItemDTO itemDTO = ItemDTO.toItemDTO(item, fileService);
    
    Page<ItemDTO> cachedPage = new PageImpl<>(List.of(itemDTO), PageRequest.of(0, 10), 1);
    
    when(itemCacheService.getItemsListFacade(categoryId, PageRequest.of(0, 10)))
        .thenReturn(cachedPage);

    // mock when returning highest bid from the bid service
    Map<String, BigDecimal> highestBidsMap = new HashMap<>();
    highestBidsMap.put(item.getId().toString(), new BigDecimal("100.00"));
    
    ApiResponse<Map<String, BigDecimal>> mockResponse = 
        new ApiResponse<>(true, "Success", highestBidsMap);

    when(bidClient.getHighestBidsForItems(any(GetHighestBidsRequest.class)))
        .thenReturn(mockResponse);

    //execute
    Page<ItemDTO> result = itemService.getAllItems(categoryId, PageRequest.of(0, 10));

    //verify
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(new BigDecimal("100.00"), result.getContent().get(0).getHighestBidPrice());
    
    verify(itemCacheService, times(1)).getItemsListFacade(categoryId, PageRequest.of(0, 10));
}

@Test
void testGetItemWhenBidClientFails() throws Exception {
    //mock file service FIRST
    when(fileService.generateDownloadPresignedUrl(item.getId() + "/pic1.png", 60))
            .thenReturn(URI.create("https://mock-s3.com/pic1.png").toURL());
    when(fileService.generateDownloadPresignedUrl(item.getId() + "/pic2.jpg", 60))
            .thenReturn(URI.create("https://mock-s3.com/pic2.jpg").toURL());

    //mokc cache service
    ItemDTO itemDTO = ItemDTO.toItemDTO(item, fileService);
    when(itemCacheService.getItemFacade(item.getId().toString())).thenReturn(itemDTO);

    // mock client fails 
    when(bidClient.getHighestBidsForItems(any(GetHighestBidsRequest.class)))
        .thenThrow(new RuntimeException("Bid service unavailable"));

    //execute
    ItemDTO result = itemService.getItem(item.getId().toString());

    // verify
    assertNotNull(result);
    assertEquals("Laptop", result.getTitle());
    assertEquals(BigDecimal.ZERO, result.getHighestBidPrice());
    
    verify(itemCacheService, times(1)).getItemFacade(item.getId().toString());
    verify(bidClient, times(1)).getHighestBidsForItems(any(GetHighestBidsRequest.class));
    verify(fileService, times(2)).generateDownloadPresignedUrl(anyString(), anyLong());
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

                // .pictureIds(List.of("pic3", "pic4"))
                .categoryId(category.getId().toString())
                .expiryTime(new Date(System.currentTimeMillis() + 200000))
                .build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item result = itemService.createItem(request, "user456");

        assertNotNull(result);
        assertEquals("Laptop", result.getTitle()); // saved mock returns "item" object
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void testSearchItem() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);

        when(itemRepository.findByTitleStartingWithIgnoreCase(
                "lap", pageable))
                .thenReturn(new PageImpl<>(List.of(item), pageable, 1));

        when(fileService.generateDownloadPresignedUrl(item.getId() + "/pic1.png",60))
                .thenReturn(URI.create("https://mock-s3.com/pic1.png").toURL());
        when(fileService.generateDownloadPresignedUrl(item.getId() + "/pic2.jpg",60))
                .thenReturn(URI.create("https://mock-s3.com/pic2.jpg").toURL());

        Page<ItemDTO> results = itemService.searchItem("lap", pageable);

        assertEquals(1, results.getNumberOfElements());
        assertEquals("Laptop", results.toList().get(0).getTitle());

        verify(itemRepository, times(1))
                .findByTitleStartingWithIgnoreCase("lap", pageable);
    }

}
