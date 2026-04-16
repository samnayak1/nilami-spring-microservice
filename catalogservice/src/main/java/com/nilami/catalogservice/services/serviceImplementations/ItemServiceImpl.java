package com.nilami.catalogservice.services.serviceImplementations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nilami.catalogservice.controllers.requestTypes.CreateItemRequestType;
import com.nilami.catalogservice.dto.ApiResponse;
import com.nilami.catalogservice.dto.GetHighestBidsRequest;
import com.nilami.catalogservice.dto.ItemDTO;
import com.nilami.catalogservice.dto.ListCacheablePage;
import com.nilami.catalogservice.dto.SimplifiedItemDTO;
import com.nilami.catalogservice.exceptions.ForbiddenException;
import com.nilami.catalogservice.exceptions.ItemNotFoundException;
import com.nilami.catalogservice.models.Category;
import com.nilami.catalogservice.models.Item;
import com.nilami.catalogservice.repositories.CategoryRepository;
import com.nilami.catalogservice.repositories.ItemRepository;
import com.nilami.catalogservice.services.externalClients.BidClient;
import com.nilami.catalogservice.services.serviceAbstractions.FileUploadService;
import com.nilami.catalogservice.services.serviceAbstractions.ItemService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final CategoryRepository categoryRepository;

    private final FileUploadService fileService;

    private final BidClient bidClient;

 

    @Override

    public Page<ItemDTO> getAllItems(String categoryId, Pageable pageable) {

        Page<ItemDTO> dtoList = this.getItemsListFacade(categoryId, pageable);

        List<UUID> itemIds = dtoList.stream()
                .map(ItemDTO::getId)
                .collect(Collectors.toList());

        Map<String, BigDecimal> highestBids = getHighestBids(itemIds);

        dtoList.forEach(item -> {
            BigDecimal highestBid = highestBids.getOrDefault(item.getId().toString(), BigDecimal.ZERO);
            item.setHighestBidPrice(highestBid);
        });

        return new PageImpl<ItemDTO>(dtoList.getContent(), pageable, dtoList.getTotalElements());

    }

    @Transactional(readOnly = true)
    @Cacheable(value = "itemFirstPage", key = "#categoryId != null ? #categoryId : 'all'", // parameter categoryId is
                                                                                           // the caching key but only
                                                                                           // on first page.
            condition = "#pageable.pageNumber == 0")
    private Page<ItemDTO> getItemsListFacade(String categoryId, Pageable pageable) {
        Page<Item> itemsPage;

        if (categoryId != null && !categoryId.isEmpty()) {
            itemsPage = itemRepository.findByCategoryId(UUID.fromString(categoryId), pageable);
        } else {

            itemsPage = itemRepository.findAll(pageable);
        }

        List<ItemDTO> dtoList = itemsPage.getContent()
                .stream()
                .map((item) -> ItemDTO.toItemDTO(item, fileService))
                .collect(Collectors.toList());

        return new ListCacheablePage<>(dtoList, pageable.getPageNumber(), pageable.getPageSize(),
                itemsPage.getTotalElements());
    }

    @Override
    public boolean checkIfExpiryDatePassed(String itemId) {
        UUID itemIdInUUID = UUID.fromString(itemId);
        Item item = itemRepository.findById(itemIdInUUID)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        return item.getExpiryTime().toInstant().isBefore(Instant.now());
    }

    // notice how it's write through cache . Means when the item is created, it
    // writes to the cache AND database.
    @Override
    @CacheEvict(value = "itemFirstPage", allEntries = true)
   public Item createItem(CreateItemRequestType request, String userId) {
        UUID categoryIdInUUID = UUID.fromString(request.getCategoryId());
        Category category = categoryRepository.findById(categoryIdInUUID)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Item item = Item.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .brand(request.getBrand())
                .creatorUserId(userId)
                .category(category)
                .location(request.getLocation())
                // .pictureIds(request.getPictureIds())
                .expiryTime(request.getExpiryTime())
                .build();

        Item savedItem = itemRepository.save(item);
        return savedItem;
    }

    @Override
    public Boolean savePictureIdsForItem(String itemId, String userId, List<String> pictureIds) {
        Optional<Item> itemDatabaseResponse = itemRepository.findById(UUID.fromString(itemId));
        if (itemDatabaseResponse.isEmpty()) {
            throw new ItemNotFoundException("Item: " + itemId + " not found");
        }

        Item item = itemDatabaseResponse.get();

        if (!item.getCreatorUserId().equals(userId)) {
            throw new ForbiddenException(userId + " not allowed to add pictures for this item");
        }

        item.setPictureIds(pictureIds);

        itemRepository.save(item);

        return true;
    }

       @Override

    public ItemDTO getItem(String itemId) {
        ItemDTO itemDTO = this.getItemFacade(itemId);

        
        List<UUID> itemIds = List.of(UUID.fromString(itemId));

        Map<String, BigDecimal> highestBids = getHighestBids(itemIds);
        BigDecimal highestBid = highestBids.getOrDefault(itemId, BigDecimal.ZERO);

        itemDTO.setHighestBidPrice(highestBid);

        return itemDTO;
    }

    @Cacheable(value = "item", key = "#itemId")
    public ItemDTO getItemFacade(String itemId) {
        UUID itemIdInUUID = UUID.fromString(itemId);
        Item item = itemRepository.findById(itemIdInUUID)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        return ItemDTO.toItemDTO(item, fileService);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemDTO> searchItem(String keyword, Pageable pageable) {
        String keywordSanitized = escapeLike(keyword);
        Page<Item> itemsPage = itemRepository
                .findByTitleStartingWithIgnoreCase(keywordSanitized, pageable);

        List<ItemDTO> dtoList = itemsPage.getContent()
                .stream()
                .map((item) -> ItemDTO.toItemDTO(item, fileService))
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, itemsPage.getTotalElements());
    }

    // to prevent sql injection. If the user input is %a%, it will search the whole
    // table and
    // it's like a DDos attack where the full table scan slows down the query
    // significantly.
    private static String escapeLike(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    @Override
    public List<SimplifiedItemDTO> getItemDetailsGivenIds(List<String> itemIds) {

        UUID[] itemUUIDs = itemIds.stream().map(itemId -> {
            return UUID.fromString(itemId);
        }).collect(Collectors.toList()).toArray(UUID[]::new);

        List<SimplifiedItemDTO> items = itemRepository.findItemsByVirtualIdList(itemUUIDs);

        return items;

    }

    private Map<String, BigDecimal> getHighestBids(List<UUID> itemIds) {
        if (itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            GetHighestBidsRequest request = new GetHighestBidsRequest(itemIds);
            ApiResponse<Map<String, BigDecimal>> response = bidClient.getHighestBidsForItems(request);

            if (!response.getSuccess() || response.getData() == null) {
                log.error("Failed to fetch highest bids: {}", response.getMessage());
                return Collections.emptyMap();
            }

            return response.getData();
        } catch (Exception e) {
            log.error("Error fetching highest bids: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

}
