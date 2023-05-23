package producer;
import domain.Item;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serializer.ItemSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemProducer {
    private static final Logger logger = LoggerFactory.getLogger(ItemProducer.class);
    String topicName = "items";
    KafkaProducer<Integer, Item> kafkaProducer;
    public ItemProducer(Map<String,Object> propsMap) {
        kafkaProducer = new KafkaProducer<>(propsMap);
    }

    public  static Map<String, Object> propsMap() {
        Map<String,Object> propsMap = new HashMap<>();
        propsMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092,localhost:9093,localhost:9094");
        propsMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        propsMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ItemSerializer.class.getName());
        propsMap.put(ProducerConfig.ACKS_CONFIG,"all");
        propsMap.put(ProducerConfig.RETRIES_CONFIG,10);
        propsMap.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG,3000);
        return propsMap;
    }
    public void close(){
        kafkaProducer.close();
    }

    public void publishMessageSync(Item item) {
        ProducerRecord<Integer,Item> producerRecord = new ProducerRecord<>(topicName,item.getId(),item);
        try {
           RecordMetadata recordMetadata = kafkaProducer.send(producerRecord).get();
            //System.out.println("partition :" + recordMetadata.partition()+ ",offset :" + recordMetadata.offset());
            logger.info("Message {} sent successfully for the key {}",item,item.getId());
            logger.info("Published message Offset is {} and the partition is {}",recordMetadata.offset(),recordMetadata.partition());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in publishMessageSync : {}",e.getMessage());
        }
    }
    public static void main(String[] args) throws InterruptedException {
        ItemProducer messageProducer = new ItemProducer(propsMap());
        Item item1 = new Item(1,"LG TV",400.00);
        Item item2 = new Item(2,"Iphone",949.49);
        List.of(item1,item2).forEach(messageProducer::publishMessageSync);
        Thread.sleep(3000);
    }
}
