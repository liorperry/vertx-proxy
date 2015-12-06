package io.vertx.example.kafka.integration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.example.util.kafka.Producer;
import io.vertx.example.web.proxy.VertxInitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SimpleKafkaProducer extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(SimpleKafkaConsumer.class);

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    private int sampleFrequence = 3000;
    private Producer producer;
    private Properties producerProperties;
    private String topic;
    private final int kafkaPort;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = VertxInitUtils.initDeploymentOptions();
        vertx.deployVerticle(new SimpleKafkaProducer("test", 9090, 3000), options);
    }

    public SimpleKafkaProducer(String topic, int kafkaPort, int sampleFrequence) {
        try {
            this.sampleFrequence = sampleFrequence;
            this.kafkaPort = kafkaPort;
            this.topic = topic;
            producerProperties = new Properties();
            producerProperties.load(getClass().getClassLoader().getResourceAsStream("producer.properties"));
            producerProperties.setProperty("bootstrap.servers", String.format("localhost:%d", kafkaPort));
            producerProperties.put("group.id", "test-group");
            producer = new Producer(producerProperties);
        } catch (IOException ioe) {
            logger.error("configuration error", ioe);
            throw new RuntimeException(ioe);
        }
    }

    public void start(Future<Void> fut) {
        vertx.setPeriodic(sampleFrequence, event -> {
            System.out.println("sending message to kafka:"+ kafkaPort);
            executor.submit(() -> {
                producer.send(topic, "message", new JsonObject(buildMessage()).encode());
            });
        });
        fut.complete();
    }

    public Map<String, Object> buildMessage() {
        Map<String, Object> map = new HashMap<>();
        map.put("publisher", "norbert");
        map.put("time", GregorianCalendar.getInstance().getTime().toLocaleString());
        map.put("readings", "{1,13,192,7,8,99,1014,4,13}");
        return map;
    }


    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        producer.shutdown();
        stopFuture.complete();
    }
}
