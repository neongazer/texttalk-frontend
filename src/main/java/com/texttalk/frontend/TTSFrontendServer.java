package com.texttalk.frontend;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.impl.JsonObjectMessage;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.platform.Verticle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Arrays.asList;

/**
 * Text-to-Speech Frontend Server Verticle
 */
public class TTSFrontendServer extends Verticle {

    private final CopyOnWriteArrayList<JsonObject> messages = new CopyOnWriteArrayList<>();
    private final Map<String, String> clientTimers = getVertx().sharedData().getMap("tts.clientTimers");

    @Override
    public void start() {

        Map<String, Object> serverConfig = new HashMap<>();
        serverConfig.put("web_root", "src/main/resources/web");
        serverConfig.put("port", 8080);
        serverConfig.put("bridge", true);
        serverConfig.put("inbound_permitted", asList(new HashMap<String, Object>()));
        serverConfig.put("outbound_permitted", asList(new HashMap<String, Object>()));

        HashMap<String, Object> sjsConfig = new HashMap<>();
        sjsConfig.put("prefix", "/tts");

        serverConfig.put("sjs_config", sjsConfig);

        container.deployModule("io.vertx~mod-web-server~2.0.0-final", new JsonObject(serverConfig));

        vertx.eventBus().registerHandler("tts/connect", new Handler<JsonObjectMessage>() {
            @Override
            public void handle(JsonObjectMessage message) {
                container.logger().info("tts/connect: " + message.body());
                message.reply(new JsonObject().putNumber("status", 200));
                vertx.eventBus().publish("tts/service", message.body());
            }
        });

        vertx.eventBus().registerHandler("tts/send", new Handler<JsonObjectMessage>() {
            @Override
            public void handle(JsonObjectMessage message) {
                container.logger().info("tts/send: " + message.body());
                messages.add(message.body());
                vertx.eventBus().publish("tts/play", message.body());
            }});

        container.logger().info("TTS Frontend Server started");
    }
}
