package io.vertx.example.kafka.integration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.example.web.proxy.VertxInitUtils;

import java.util.Optional;


public class KafkaServerVertical extends AbstractVerticle {
    public static final String KAFKA_PORT = "KAFKA.PORT";
    public static final String ZK_PORT = "ZOOKEEPER.PORT";

    private int kafkaPort;
    private int zkPort;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = VertxInitUtils.initDeploymentOptions();
        vertx.deployVerticle(new KafkaServerVertical(2181,9090),
                options);

    }

    private Optional<EmbeddedKafkaServer> kafkaServer = Optional.empty();

    public KafkaServerVertical(EmbeddedKafkaServer kafkaServer) {
        this.kafkaServer = Optional.of(kafkaServer);
        launchKafka();
    }

    public KafkaServerVertical(int zkPort , int kafkaPort) {
        this.kafkaPort = kafkaPort;
        this.zkPort = zkPort;
        launchKafka();
    }

    public void start(Future<Void> fut) {
        fut.complete();
    }

    private void launchKafka() {
        if(!kafkaServer.isPresent()) {
            kafkaServer = Optional.of(new EmbeddedKafkaServer(zkPort,kafkaPort));
        }
        System.out.println("starting kafka embedded server");
        kafkaServer.get().start();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        kafkaServer.get().stop();
        stopFuture.complete();
    }
}
