import { Kafka } from 'kafkajs';
import { KafkaConsumerGroups, KafkaTopics } from './enums/kafkaEnums';
import { BidEventSchema } from './validators/bidAddedPayloadValidator';


export const kafka = new Kafka({
  clientId: 'websocket-service',
  brokers: [process.env.KAFKA_BROKER],
  ssl: false,
  sasl: undefined,
});

export const consumer = kafka.consumer({ groupId: KafkaConsumerGroups.ItemBidConsumer });


//takes in a function as parameter.
export const startConsumer = async (onBid: (event: any) => void) => {
  await consumer.connect();
  await consumer.subscribe({
    topic: KafkaTopics.ItemBid,
    fromBeginning: false
  });

  consumer.run({
    eachMessage: async ({ message }) => {
      const parsed = JSON.parse(message.value!.toString());

      const event = BidEventSchema.parse(parsed);

      onBid(event);
    }
  });
}
