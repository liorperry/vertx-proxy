package io.vertx.example.kafka.integration;

import io.vertx.example.util.kafka.Consumer;
import io.vertx.example.util.kafka.KafkaLocal;
import io.vertx.example.util.kafka.Producer;
import io.vertx.example.kafka.ZookeeperLocal;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Future;

public class EmbeddedKafkaServer {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedKafkaServer.class);

    private final Properties zookeeperProperties = new Properties();
    private final Properties kafkaProperties = new Properties();
    private final Properties producerProperties = new Properties();
    private final Properties consumerProperties = new Properties();

    private final KafkaLocal kafkaLocal;
    private final ZookeeperLocal zookeeperLocal;
    private final Producer producer;
    private final Map<String, Consumer> consumers = new HashMap<>();
    private final Integer zookeeperPort;
    private final Integer kafkaPort;

    public static void main(String[] args) {
        EmbeddedKafkaServer server = new EmbeddedKafkaServer(2181,9090);
        server.start();
/*
        Optional<String> test = server.read("test");
        System.out.println("reading before value set : " + test);
        Future<RecordMetadata> send = server.send("test", "key", "value");
        test = server.read("test");
        System.out.println("reading after value set : " + test);
        server.stop();
*/
    }

    public EmbeddedKafkaServer(Integer zookeeperPort, Integer kafkaPort){
        this.zookeeperPort = zookeeperPort;
        this.kafkaPort = kafkaPort;
        try {
            zookeeperProperties.load(getClass().getClassLoader().getResourceAsStream("zookeeper.properties"));
            zookeeperProperties.setProperty("clientPort", zookeeperPort.toString());

            kafkaProperties.load(getClass().getClassLoader().getResourceAsStream("kafka.properties"));
            kafkaProperties.setProperty("port", kafkaPort.toString());
            kafkaProperties.setProperty("zookeeper.connect", String.format("localhost:%d", zookeeperPort));

            producerProperties.load(getClass().getClassLoader().getResourceAsStream("producer.properties"));
            producerProperties.setProperty("bootstrap.servers", String.format("localhost:%d", kafkaPort));

            consumerProperties.load(getClass().getClassLoader().getResourceAsStream("consumer.properties"));
            consumerProperties.setProperty("zookeeper.connect", String.format("localhost:%d", zookeeperPort));

            zookeeperLocal = new ZookeeperLocal(zookeeperProperties);
            kafkaLocal = new KafkaLocal(kafkaProperties);
            producer = new Producer(producerProperties);
        } catch(IOException ioe){
            logger.error("configuration error", ioe);
            throw new RuntimeException(ioe);
        }
    }

    public void start(){
        System.out.println("Starting local zookeeper:"+zookeeperPort);
        zookeeperLocal.start();
        System.out.println("Starting local kafka:" + kafkaPort);
        kafkaLocal.start();
    }

    public void stop(){
        producer.shutdown();
        consumers.forEach((k, v) -> v.shutdown());
        kafkaLocal.stop();
        zookeeperLocal.stop();
    }

    public Future<RecordMetadata> send(String topic, String key, String value){
        return producer.send(topic, key, value);
    }

    public Optional<String> read(String topic){
        return consumers.computeIfAbsent(topic, t -> new Consumer(consumerProperties, t)).read();
    }

}
