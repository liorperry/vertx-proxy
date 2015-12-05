package io.vertx.example.kafka;

/**
 * Created by lior on 05/12/2015.
 */

import com.google.common.collect.ImmutableMap;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;

public class Consumer {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private final ConsumerConnector consumerConnector;
    private ConsumerIterator<byte[], byte[]> iterator;

    public Consumer(Properties consumerProperties, String topic){
        consumerConnector = kafka.consumer.Consumer
                .createJavaConsumerConnector(new ConsumerConfig(consumerProperties));
        KafkaStream<byte[], byte[]> stream = consumerConnector
                .createMessageStreams(ImmutableMap.of(topic, 1)).get(topic).get(0);
        iterator = stream.iterator();
    }

    public void shutdown(){
        logger.debug("shutdown consumer");
        consumerConnector.shutdown();
    }

    public Optional<String> read(){
        return Optional.of(iterator)
                .filter(this::hasMessage)
                .map(i -> new String(iterator.next().message()));
    }

    public boolean hasMessage(ConsumerIterator<byte[], byte[]> iterator){
        try {
            return iterator.hasNext();
        } catch(ConsumerTimeoutException cte){
            logger.debug("no message found in the queue", cte);
            return false;
        }
    }
}
