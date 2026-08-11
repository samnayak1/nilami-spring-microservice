
package com.nilami.catalogservice.services.serviceImplementations;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nilami.catalogservice.dto.ItemDTO;
import com.nilami.catalogservice.dto.ListCacheablePage;
import com.nilami.catalogservice.models.Item;
import com.nilami.catalogservice.repositories.ItemRepository;
import com.nilami.catalogservice.services.serviceAbstractions.FileUploadService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemCacheService {

    private final ItemRepository itemRepository;

   private final FileUploadService fileService;

    @Transactional(readOnly = true)
    @Cacheable(
        value = "itemFirstPage",
        key = "#categoryId != null ? #categoryId : 'all'",
        condition = "#pageable.pageNumber == 0"
    )
    public Page<ItemDTO> getItemsListFacade(String categoryId, Pageable pageable) {
        Page<Item> itemsPage;

        if (categoryId != null && !categoryId.isEmpty()) {
            itemsPage = itemRepository.findByCategoryId(UUID.fromString(categoryId), pageable);
        } else {
            itemsPage = itemRepository.findAll(pageable);
        }

        List<ItemDTO> dtoList = itemsPage.getContent()
                .stream()
                .map(item -> ItemDTO.toItemDTO(item, fileService))
                .collect(Collectors.toList());

        return new ListCacheablePage<>(dtoList, pageable.getPageNumber(), pageable.getPageSize(),
                itemsPage.getTotalElements());
    }

@Cacheable(value = "item", key = "#itemId")
    public ItemDTO getItemFacade(String itemId) {
        UUID itemIdInUUID = UUID.fromString(itemId);
        Item item = itemRepository.findById(itemIdInUUID)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        return ItemDTO.toItemDTO(item, fileService);
    }
}