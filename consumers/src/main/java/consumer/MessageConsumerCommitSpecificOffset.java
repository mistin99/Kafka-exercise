package consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageConsumerCommitSpecificOffset {
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumerCommitSpecificOffset.class);
    private final Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap = new HashMap<>();
    KafkaConsumer<String,String> kafkaConsumer;
    String topicName = "test-topic-replicated";
    public MessageConsumerCommitSpecificOffset(Map<String,Object> propsMap) {
        kafkaConsumer = new KafkaConsumer<>(propsMap);
    }
    public static Map<String,Object> buildConsumerProperties() {
        Map<String,Object> propsMap = new HashMap<>();
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092,localhost:9093,localhost:9094");
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG,"MessageConsumer2");
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"false");
        //propsMap.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,"5000");
        //propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,1000);
        return propsMap;
    }
    public void pollKafka() {
        kafkaConsumer.subscribe(List.of(topicName));
        Duration timeOutDuration = Duration.of(100, ChronoUnit.MILLIS);
        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(timeOutDuration);
                consumerRecords.forEach((record) -> {
                    logger.info("Consumer Record key is {} and the value is {} and the partition is {}", record.key(), record.value(), record.partition());
                    offsetAndMetadataMap.put(new TopicPartition(record.topic(),record.partition()),new OffsetAndMetadata(record.offset()+1,null));
                });
                if (consumerRecords.count()>0) {
                    kafkaConsumer.commitSync(offsetAndMetadataMap);
                    logger.info("Offset is committed");
                }
            }
        }

        catch (CommitFailedException e) {
            logger.error("CommitFailedException in pollKafka :" + e);
        }
        catch (Exception e) {
            logger.error("Exception in pollKafka :" + e);
        }
        finally {
            kafkaConsumer.close();
        }

    }
    public static void main(String[] args) {
        MessageConsumerCommitSpecificOffset messageConsumer = new MessageConsumerCommitSpecificOffset(buildConsumerProperties());
        messageConsumer.pollKafka();
    }
}
