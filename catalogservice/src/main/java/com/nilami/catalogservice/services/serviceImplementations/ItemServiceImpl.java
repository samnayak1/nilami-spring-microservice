package com.nilami.catalogservice.services.serviceImplementations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nilami.catalogservice.controllers.requestTypes.CreateItemRequestType;
import com.nilami.catalogservice.dto.ApiResponse;
import com.nilami.catalogservice.dto.GetHighestBidsRequest;
import com.nilami.catalogservice.dto.ItemDTO;
import com.nilami.catalogservice.dto.SimplifiedItemDTO;
import com.nilami.catalogservice.exceptions.ItemNotFoundException;
import com.nilami.catalogservice.models.Category;
import com.nilami.catalogservice.models.Item;
import com.nilami.catalogservice.repositories.CategoryRepository;
import com.nilami.catalogservice.repositories.ItemRepository;
import com.nilami.catalogservice.services.externalClients.BidClient;
import com.nilami.catalogservice.services.serviceAbstractions.FileUploadService;
import com.nilami.catalogservice.services.serviceAbstractions.ItemService;

import jakarta.ws.rs.ForbiddenException;
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
    public ItemDTO getItem(String itemId) {
        UUID itemIdInUUID = UUID.fromString(itemId);
        Item item = itemRepository.findById(itemIdInUUID)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        return ItemDTO.toItemDTO(item, fileService);
    }
 
    @Override
    public Page<ItemDTO> getAllItems(String categoryId,Pageable pageable) {

    Page<Item> itemsPage;

    if(categoryId!=null&& !categoryId.isEmpty()){
          itemsPage = itemRepository.findByCategoryId(categoryId, pageable);
    } else{

           itemsPage=itemRepository.findAll(pageable);
    }

       
        List<ItemDTO> dtoList = itemsPage.getContent()
                .stream()
                .map((item) -> ItemDTO.toItemDTO(item, fileService))
                .collect(Collectors.toList());

        List<UUID> itemIds = dtoList.stream()
            .map(ItemDTO::getId)
            .collect(Collectors.toList());

        Map<String, BigDecimal> highestBids = getHighestBids(itemIds);
        
        dtoList.forEach(item -> {
         BigDecimal highestBid = highestBids.getOrDefault(item.getId().toString(), BigDecimal.ZERO);
         item.setHighestBidPrice(highestBid);
    });

        
        return new PageImpl<>(dtoList, pageable, itemsPage.getTotalElements());
    }

    @Override
    public boolean checkIfExpiryDatePassed(String itemId) {
        UUID itemIdInUUID = UUID.fromString(itemId);
        Item item = itemRepository.findById(itemIdInUUID)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        return item.getExpiryTime().toInstant().isBefore(Instant.now());
    }

    @Override
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
    public Page<ItemDTO> searchItem(String keyword, Pageable pageable) {
        Page<Item> itemsPage = itemRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);

        List<ItemDTO> dtoList = itemsPage.getContent()
                .stream()
                .map((item) -> ItemDTO.toItemDTO(item, fileService))
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, itemsPage.getTotalElements());
    }

    @Override
    public List<SimplifiedItemDTO> getItemDetailsGivenIds(List<String> itemIds) {
                 
               UUID[] itemUUIDs=itemIds.stream().map(itemId->{
                return UUID.fromString(itemId);
               }).collect(Collectors.toList()).toArray(UUID[]::new);
                
               List<SimplifiedItemDTO> items=itemRepository.findItemsByVirtualIdList(itemUUIDs);

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
