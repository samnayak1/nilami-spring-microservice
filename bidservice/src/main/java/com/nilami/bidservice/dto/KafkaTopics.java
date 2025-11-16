package com.nilami.bidservice.dto;

public enum KafkaTopics {

    ITEM_BID("item-bid");

    private final String topicName;

    KafkaTopics(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }
}