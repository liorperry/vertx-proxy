package io.vertx.example.kafka;

/**
 * Created by lior on 05/12/2015.
 */
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZookeeperLocal {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperLocal.class);

    private final ZooKeeperServerMain zooKeeperServer;
    private final ServerConfig configuration;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ZookeeperLocal(Properties properties) {
        QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();

        try {
            quorumConfiguration.parseProperties(properties);
        } catch (Exception e) {
            logger.error("zookeeper local configuration error", e);
            throw new RuntimeException(e);
        }

        configuration = new ServerConfig();
        configuration.readFrom(quorumConfiguration);
        zooKeeperServer = new ZooKeeperServerMain();
    }

    public void start() {
        executorService.submit(() -> {
            try {
                zooKeeperServer.runFromConfig(configuration);
            } catch (IOException e) {
                logger.error("zookeeper local configuration error", e);
            }
        });
    }

    public void stop() {
        executorService.shutdown();
    }
}
