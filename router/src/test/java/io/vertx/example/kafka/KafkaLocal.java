package io.vertx.example.kafka;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaLocal {
    private static final Logger logger = LoggerFactory.getLogger(KafkaLocal.class);

    private final KafkaServerStartable kafkaServer;

    public KafkaLocal(Properties properties){
        KafkaConfig kafkaConfig = new KafkaConfig(properties);
        kafkaServer = new KafkaServerStartable(kafkaConfig);
    }

    public void start(){
        logger.debug("start kafka local");
        kafkaServer.startup();
        logger.debug("kafka local started");
    }

    public void stop(){
        logger.debug("stop kafka local");
        kafkaServer.shutdown();
        logger.debug("kafka local stopped");
    }
}
