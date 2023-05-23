package consumer;

import listeners.MessageRebalancedListener;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageConsumerRebalancedListener {
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumerRebalancedListener.class);
    KafkaConsumer<String,String> kafkaConsumer;
    String topicName = "test-topic-replicated";
    public MessageConsumerRebalancedListener(Map<String,Object> propsMap) {
        kafkaConsumer = new KafkaConsumer<>(propsMap);
    }
    public static Map<String,Object> buildConsumerProperties() {
        Map<String,Object> propsMap = new HashMap<>();
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092,localhost:9093,localhost:9094");
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG,"MessageConsumer2");
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        propsMap.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,"5000");
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"false");
        return propsMap;
    }
    public void pollKafka() {
        kafkaConsumer.subscribe(List.of(topicName),new MessageRebalancedListener(kafkaConsumer));
        Duration timeOutDuration = Duration.of(100, ChronoUnit.MILLIS);
        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(timeOutDuration);
                consumerRecords.forEach((record) -> {
                    logger.info("Consumer Record key is {} and the value is {} and the partition is {}", record.key(), record.value(), record.partition());
                });
                kafkaConsumer.commitSync();
            }
        }

        catch (Exception e) {
            logger.error("Exception in pollKafka :" + e);
        }
        finally {
            kafkaConsumer.close();
        }

    }
    public static void main(String[] args) {
        MessageConsumerRebalancedListener messageConsumer = new MessageConsumerRebalancedListener(buildConsumerProperties());
        messageConsumer.pollKafka();
    }
}
