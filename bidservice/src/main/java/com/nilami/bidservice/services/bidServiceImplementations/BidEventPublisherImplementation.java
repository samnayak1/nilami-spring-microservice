package com.nilami.bidservice.services.bidServiceImplementations;


import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import com.nilami.bidservice.dto.BidEventMessageQueuePayload;
import com.nilami.bidservice.dto.KafkaTopics;
import com.nilami.bidservice.services.BidEventPublisher;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j  
public class BidEventPublisherImplementation implements BidEventPublisher {

    private final KafkaTemplate<String, BidEventMessageQueuePayload> kafkaTemplate;
    private static final String TOPIC = KafkaTopics.ITEM_BID.getTopicName();

   
    public BidEventPublisherImplementation(
            KafkaTemplate<String, BidEventMessageQueuePayload> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendBidEventToQueue(BidEventMessageQueuePayload bidPayload) {
        try {
            log.debug("Sending bid event to topic: {}", TOPIC);
            
            kafkaTemplate.send(TOPIC, bidPayload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Bid event sent successfully: {}", 
                            result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send bid event", ex);
                    }
                });
                
        } catch (Exception e) {
            log.error("Error sending bid event to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish bid event", e);
        }
    }
}
