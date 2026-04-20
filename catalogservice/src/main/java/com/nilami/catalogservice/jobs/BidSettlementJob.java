package com.nilami.catalogservice.jobs;

import java.math.BigDecimal;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nilami.catalogservice.dto.GetHighestBidAlongWithItemIds;
import com.nilami.catalogservice.models.Item;
import com.nilami.catalogservice.models.ItemSettlement;
import com.nilami.catalogservice.repositories.ItemRepository;
import com.nilami.catalogservice.repositories.ItemSettlementRepository;
import com.nilami.catalogservice.services.serviceAbstractions.ItemService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class BidSettlementJob {
    private final ItemRepository itemRepository;
    private final ItemService itemService;
    private final ItemSettlementRepository itemSettlementRepository;

    @Scheduled(cron = "0 */1 * * * *")  //1 mins
    public void settleExpiredItems() {
        log.info("BidSettlementJob started");
        try {
            List<Item> expiredItems = itemRepository
                .findByExpiryTimeBeforeAndSettledFalse(new Date());

            if (expiredItems.isEmpty()) {
                log.info("BidSettlementJob: no expired items to settle");
                return;
            }

            log.info("BidSettlementJob: found {} expired items to settle", expiredItems.size());

            List<UUID> expiredItemIds = expiredItems.stream()
                    .map(Item::getId)
                    .collect(Collectors.toList());

            Map<String, GetHighestBidAlongWithItemIds> highestBids = 
                itemService.getHighestBidsAlongWithUserId(expiredItemIds);

            List<ItemSettlement> winningBids = expiredItems.stream()
                    .map(item -> {
                        GetHighestBidAlongWithItemIds bidInfo = highestBids.get(item.getId().toString());
                        BigDecimal highestBid = bidInfo != null ? bidInfo.getHighestBidPrice() : BigDecimal.ZERO;
                        UUID userId = bidInfo != null ? bidInfo.getUserId() : null;
                        return ItemSettlement.builder()
                                .itemId(item.getId())
                                .userId(userId)
                                .winningBidAmount(highestBid)
                                .build();
                    })
                    .filter(wb -> wb.getWinningBidAmount().compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());

            log.info("BidSettlementJob: {} items have winning bids, settling...", winningBids.size());
            setItemsAsSettled(expiredItemIds, winningBids);
            log.info("BidSettlementJob: successfully settled {} items", winningBids.size());

        } catch (Exception e) {
            log.error("BidSettlementJob: failed to settle expired items - {}", e.getMessage(), e);
        }
    }

  @Transactional
void setItemsAsSettled(List<UUID> itemIds, List<ItemSettlement> winningBids) {
    try {
        List<Item> itemsToSettle = itemRepository.findAllById(itemIds);
        itemsToSettle.forEach(item -> item.setSettled(true));
        itemRepository.saveAll(itemsToSettle); 
        itemSettlementRepository.saveAll(winningBids);
        log.info("BidSettlementJob: transaction committed for {} items", itemIds.size());
    } catch (Exception e) {
        log.error("BidSettlementJob: transaction failed for itemIds {} - {}", itemIds, e.getMessage(), e);
        throw e;
    }
}
}
