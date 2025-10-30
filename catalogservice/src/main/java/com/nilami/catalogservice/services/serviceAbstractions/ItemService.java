package com.nilami.catalogservice.services.serviceAbstractions;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nilami.catalogservice.controllers.requestTypes.CreateItemRequestType;
import com.nilami.catalogservice.dto.ItemDTO;
import com.nilami.catalogservice.models.Item;

public interface ItemService{
    ItemDTO getItem(String itemId);

    Page<ItemDTO> getAllItems(Pageable pageable);

    boolean checkIfExpiryDatePassed(String itemId);

    Item createItem(CreateItemRequestType request,String userId);

    Page<ItemDTO> searchItem(String keyword, Pageable pageable);

    Boolean savePictureIdsForItem(String itemId,String userId, List<String> pictureIds);

}