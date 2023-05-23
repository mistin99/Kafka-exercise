package producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MessageProducer {
    private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);
    String topicName = "test-topic-replicated";
    KafkaProducer<String,String> kafkaProducer;
    public  MessageProducer(Map<String,Object> propsMap) {
        kafkaProducer = new KafkaProducer<>(propsMap);
    }

    public  static Map<String, Object> propsMap() {
        Map<String,Object> propsMap = new HashMap<>();
        propsMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092,localhost:9093,localhost:9094");
        propsMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propsMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        propsMap.put(ProducerConfig.ACKS_CONFIG,"all");
        propsMap.put(ProducerConfig.RETRIES_CONFIG,10);
        propsMap.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG,3000);
        return propsMap;
    }
    public void close(){
        kafkaProducer.close();
    }

    public void publishMessageSync(String key,String value) {
        ProducerRecord<String,String> producerRecord = new ProducerRecord<>(topicName,key,value);
        try {
           RecordMetadata recordMetadata = kafkaProducer.send(producerRecord).get();
            //System.out.println("partition :" + recordMetadata.partition()+ ",offset :" + recordMetadata.offset());
            logger.info("Message {} sent successfully for the key {}",value,key);
            logger.info("Published message Offset is {} and the partition is {}",recordMetadata.offset(),recordMetadata.partition());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in publishMessageSync : {}",e.getMessage());
        }
    }
    public static void main(String[] args) throws InterruptedException {
        MessageProducer messageProducer = new MessageProducer(propsMap());
        messageProducer.publishMessageSync("99","Haha1");
        messageProducer.publishMessageSync("9","Haha12");
        Thread.sleep(3000);
    }
}
