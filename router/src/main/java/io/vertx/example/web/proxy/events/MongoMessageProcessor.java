package io.vertx.example.web.proxy.events;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MongoMessageProcessor implements MessageProcessor {
    private MongoClient client;

    public MongoMessageProcessor(MongoClient client) {
        this.client = client;
    }

    @Override
    public void onMessage(String channel, String message) {
        addToStorage(channel,new JsonObject(message));
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {

    }

    private void addToStorage(String channel,JsonObject log) {
        client.save(channel +"." + log.getString("user"), log, id -> {
            System.out.println("Inserted id: " + id.result());
        });
    }
}
