package com.nilami.bidservice.services;

import com.nilami.bidservice.dto.BidEventMessageQueuePayload;

public interface BidEventPublisher {
    public void sendBidEventToQueue(BidEventMessageQueuePayload bidPayload);
}
