package io.vertx.example.kafka;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.example.web.proxy.VertxInitUtils;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static io.vertx.example.web.proxy.VertxInitUtils.getNextAvailablePort;
import static io.vertx.example.web.proxy.VertxInitUtils.initOptions;

@RunWith(VertxUnitRunner.class)
public class SimpleKafkaVerticalTest {

    public static final String LOCALHOST = "localhost";
    static DeploymentOptions options = VertxInitUtils.initDeploymentOptions();
    static Vertx vertx;

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        int zkPort = getNextAvailablePort();
        int kafkaPort = getNextAvailablePort();
        options.getConfig().put(KafkaServerVertical.ZK_PORT, zkPort);
        options.getConfig().put(KafkaServerVertical.KAFKA_PORT, kafkaPort);

        vertx = Vertx.vertx(initOptions());

        vertx.deployVerticle(new KafkaServerVertical(kafkaPort,zkPort),
                            options,
                            context.asyncAssertSuccess());

        vertx.deployVerticle(new SimpleKafkaConsumer("topic",zkPort),
                options,
                context.asyncAssertSuccess());

        vertx.deployVerticle(new SimpleKafkaProducer("topic",kafkaPort),
                options,
                context.asyncAssertSuccess());
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void testKafkaMessageProduction(TestContext context) {


    }


}
