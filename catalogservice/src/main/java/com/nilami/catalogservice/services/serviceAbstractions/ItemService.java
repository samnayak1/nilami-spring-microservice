package com.nilami.catalogservice.services.serviceAbstractions;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nilami.catalogservice.controllers.requestTypes.CreateItemRequestType;
import com.nilami.dto.ItemDTO;

public interface ItemService{
    ItemDTO getItem(String itemId);

    Page<ItemDTO> getAllItems(Pageable pageable);

    boolean checkIfExpiryDatePassed(String itemId);

    ItemDTO createItem(CreateItemRequestType request);

    Page<ItemDTO> searchItem(String keyword, Pageable pageable);

}