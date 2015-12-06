package io.vertx.example.util.kafka;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Future;

public class Producer {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    private final KafkaProducer<String,String> producer;

    public Producer(Properties properties){
        producer = new KafkaProducer<>(properties);
    }

    public Future<RecordMetadata> send(String topic, String key, String value){
        System.out.println("sending message " + value + " for key " + key + " on topic " + topic);
        ProducerRecord<String,String> producerRecord = new ProducerRecord<>(topic, key, value);
        return producer.send(producerRecord);
    }

    public void shutdown(){
        logger.debug("shutdown producer");
        producer.close();
    }
}
