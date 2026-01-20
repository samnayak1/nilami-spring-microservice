package com.nilami.bidservice.services.bidServiceImplementations;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nilami.bidservice.dto.BidEventMessageQueuePayload;
import com.nilami.bidservice.dto.OutboxStatus;
import com.nilami.bidservice.models.OutboxEvent;
import com.nilami.bidservice.repositories.OutboxRepository;
import com.nilami.bidservice.services.BidEventPublisher;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutboxRepository outboxEventRepository;
    private final BidEventPublisher bidEventPublisher;
    private final ObjectMapper objectMapper;


    //Debugging: to check if JavaTimeModule is present. 
    @PostConstruct
public void verifyMapper() {
    log.info("ObjectMapper modules: {}", objectMapper.getRegisteredModuleIds());
}

    @Transactional
    @Scheduled(fixedDelayString = "5000")
    public void publishOutboxEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findTop50ByStatusOrderByCreatedAt(OutboxStatus.NEW);

        for (OutboxEvent event : events) {
            try {
                BidEventMessageQueuePayload payload =
                        objectMapper.readValue(
                                event.getPayload(),
                                BidEventMessageQueuePayload.class
                        );

                bidEventPublisher.sendBidEventToQueue(payload);

                event.setStatus(OutboxStatus.SENT);
                outboxEventRepository.save(event); 
            } catch (Exception e) {
                log.warn("Failed to publish outbox event {}", event.getId(), e);
                event.setStatus(OutboxStatus.FAILED);
                outboxEventRepository.save(event);
            }
        }
    }
}
