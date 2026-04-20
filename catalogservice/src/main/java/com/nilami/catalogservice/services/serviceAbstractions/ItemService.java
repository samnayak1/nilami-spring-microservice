package com.nilami.catalogservice.services.serviceAbstractions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nilami.catalogservice.controllers.requestTypes.CreateItemRequestType;
import com.nilami.catalogservice.dto.GetHighestBidAlongWithItemIds;
import com.nilami.catalogservice.dto.ItemDTO;
import com.nilami.catalogservice.dto.SimplifiedItemDTO;
import com.nilami.catalogservice.models.Item;

public interface ItemService{
    ItemDTO getItem(String itemId);

    Page<ItemDTO> getAllItems(String categoryId,Pageable pageable);

    boolean checkIfExpiryDatePassed(String itemId);

    Item createItem(CreateItemRequestType request,String userId);

    List<SimplifiedItemDTO> getItemDetailsGivenIds(List<String> itemIds);

    Page<ItemDTO> searchItem(String keyword, Pageable pageable);

    Boolean savePictureIdsForItem(String itemId,String userId, List<String> pictureIds);

    Map<String, BigDecimal> getHighestBids(List<UUID> itemIds);

    Map<String, GetHighestBidAlongWithItemIds> getHighestBidsAlongWithUserId(List<UUID> itemIds);

}