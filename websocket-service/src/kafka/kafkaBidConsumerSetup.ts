import { Kafka } from 'kafkajs';
import { KafkaConsumerGroups, KafkaTopics } from './enums/kafkaEnums';
import { BidEvent, BidEventSchema } from './validators/bidAddedPayloadValidator';


export const kafka = new Kafka({
  clientId: 'websocket-service',
  brokers: [process.env.KAFKA_BROKER],
  ssl: false,
  sasl: undefined,
  logLevel: 2 
});

export const consumer = kafka.consumer({ groupId: KafkaConsumerGroups.ItemBidConsumer });
//For 3 consumers in a consumer group, you can spin up 3 different node servers. 
//because we have 3 partitions in topic KafkaTopics.ItemBid
//where one consumer reads one topic partition
//However, for our case, we will only have one consumer.
//takes in a function as parameter.


//NOTE: To have 3 different consumers in the same consumer group, the groupId should be the same
//In our case it will be KafkaConsumerGroups.ItemBidConsumer.

//backpressure is when consumers read slower than producers. For our case, we don't have many requests so it's fine.
export const startBidConsumer = async (onBid: (event: BidEvent) => void) => {
  await consumer.connect();
  await consumer.subscribe({
    topic: KafkaTopics.ItemBid,
    fromBeginning: false
  });

  consumer.run({
    eachMessage: async ({partition, topic, message }) => {
    
      const parsed = JSON.parse(message.value!.toString());
      const event = BidEventSchema.parse(parsed);
      onBid(event);
    }
  });
}
