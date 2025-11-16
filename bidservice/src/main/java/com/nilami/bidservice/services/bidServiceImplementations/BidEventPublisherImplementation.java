package com.nilami.bidservice.services.bidServiceImplementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import com.nilami.bidservice.dto.BidEventMessageQueuePayload;
import com.nilami.bidservice.dto.KafkaTopics;
import com.nilami.bidservice.services.BidEventPublisher;


@Service
public class BidEventPublisherImplementation implements BidEventPublisher{

    @Autowired
    private KafkaTemplate<String, BidEventMessageQueuePayload> kafkaTemplate;

    private static final String TOPIC = KafkaTopics.ITEM_BID.getTopicName();

    @Override
    public void sendBidEventToQueue(BidEventMessageQueuePayload bidPayload) {
          kafkaTemplate.send(TOPIC, bidPayload);
    }
    


}
