package com.nilami.catalogservice.services.serviceImplementations;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nilami.catalogservice.controllers.requestTypes.CreateItemRequestType;
import com.nilami.catalogservice.dto.ItemDTO;
import com.nilami.catalogservice.models.Category;
import com.nilami.catalogservice.models.Item;
import com.nilami.catalogservice.repositories.CategoryRepository;
import com.nilami.catalogservice.repositories.ItemRepository;
import com.nilami.catalogservice.services.serviceAbstractions.FileUploadService;
import com.nilami.catalogservice.services.serviceAbstractions.ItemService;

import lombok.RequiredArgsConstructor;


    
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
     
    private final ItemRepository itemRepository;

    private final CategoryRepository categoryRepository;

    private final FileUploadService fileService;

    @Override
    public ItemDTO getItem(String itemId) {
        UUID itemIdInUUID=UUID.fromString(itemId);
        Item item = itemRepository.findById(itemIdInUUID)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        return ItemDTO.toItemDTO(item,fileService);
    }

    @Override
    public Page<ItemDTO> getAllItems(Pageable pageable) {
        Page<Item> itemsPage = itemRepository.findAll(pageable);
        List<ItemDTO> dtoList = itemsPage.getContent()
                .stream()
                .map((item)->ItemDTO.toItemDTO(item,fileService))
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, itemsPage.getTotalElements());
    }

    @Override
    public boolean checkIfExpiryDatePassed(String itemId) {
         UUID itemIdInUUID=UUID.fromString(itemId);
        Item item = itemRepository.findById(itemIdInUUID)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        return item.getExpiryTime().toInstant().isBefore(Instant.now());
    }

    @Override
    public ItemDTO createItem(CreateItemRequestType request,String userId) {
         UUID categoryIdInUUID=UUID.fromString(request.getCategoryId());
        Category category=categoryRepository.findById(categoryIdInUUID)
            .orElseThrow(() -> new RuntimeException("Category not found"));


        Item item = Item.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .brand(request.getBrand())
                .creatorUserId(userId)
                .category(category)
                .pictureIds(request.getPictureIds())
                .expiryTime(request.getExpiryTime())
                .build();

        Item savedItem = itemRepository.save(item);
        return ItemDTO.toItemDTO(savedItem,fileService);
    }

    @Override
    public Page<ItemDTO> searchItem(String keyword, Pageable pageable) {
        Page<Item> itemsPage = itemRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);

        List<ItemDTO> dtoList = itemsPage.getContent()
                .stream()
                .map((item)->ItemDTO.toItemDTO(item,fileService))
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, itemsPage.getTotalElements());
    }
}

