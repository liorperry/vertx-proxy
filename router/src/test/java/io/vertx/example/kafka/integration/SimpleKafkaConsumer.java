package io.vertx.example.kafka.integration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.example.util.kafka.*;
import io.vertx.example.web.proxy.VertxInitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class SimpleKafkaConsumer extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(SimpleKafkaConsumer.class);

    private int sampleFrequence = 3000;

    private Consumer consumer;
    private SamplePersister persister;
    private SampleExtractor extractor;
    private int zkPort;
    private Properties consumerProperties;
    private String topic;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = VertxInitUtils.initDeploymentOptions();
        BasicSampleExtractor extractor = new BasicSampleExtractor();
        vertx.deployVerticle(new SimpleKafkaConsumer(
                new RedisSamplePersister(new JedisPool(), extractor),
                extractor,
                "test", 2181, 3000), options);
    }

    public SimpleKafkaConsumer(SamplePersister persister,
                               SampleExtractor extractor,
                               String topic, int zkPort,
                               int sampleFrequence) {
        this.persister = persister;
        this.extractor = extractor;
        this.zkPort = zkPort;
        this.sampleFrequence = sampleFrequence;
        try {
            this.topic = topic;
            consumerProperties = new Properties();
            consumerProperties.load(getClass().getClassLoader().getResourceAsStream("consumer.properties"));
            consumerProperties.setProperty("zookeeper.connect", String.format("localhost:%d", zkPort));
            consumerProperties.put("group.id", "test-group");
            consumer = new Consumer(consumerProperties, topic);
        } catch (IOException ioe) {
            logger.error("configuration error", ioe);
            throw new RuntimeException(ioe);
        }

    }

    public void start(Future<Void> fut) throws Exception {
        super.start();
        vertx.setPeriodic(sampleFrequence, event -> {
            System.out.println("reading message from kafka vis Zookeeper:" + zkPort + " consumer on topic:" + topic);
            Optional<String> value = consumer.read();
            System.out.println(value);
            if (value.isPresent()) {
                Optional<SampleData> sampleData = Optional.empty();
                try {
                    sampleData = extractor.extractSample(new JsonObject(value.get()));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Extractor could not extract data from sample["+value.get()+"]");
                }
                if (sampleData.isPresent())
                    persister.persist(sampleData.get());
            }
        });
        fut.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        consumer.shutdown();
        stopFuture.complete();
    }
}
