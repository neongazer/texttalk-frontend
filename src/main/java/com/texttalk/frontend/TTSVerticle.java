package com.texttalk.frontend;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

public class TTSVerticle extends Verticle {

    Logger logger = null;

    @Override
    public void start() {

        logger = container.logger();
        logger.info("Running Verticle 2...");

        JsonObject config = container.config();
        Long timeout = config.getLong("timeout", 2 * 60 * 1000);
        Long latencyTimeout = config.getLong("latencyTimeout", 5 * 1000);

        Map<String, Object> serverConfig = new HashMap<>();
        serverConfig.put("port", 8080);
        serverConfig.put("bridge", true);
        serverConfig.put("inbound_permitted", asList(new HashMap<String, Object>()));
        serverConfig.put("outbound_permitted", asList(new HashMap<String, Object>()));
        HashMap<String, Object> sjsConfig = new HashMap<>();
        sjsConfig.put("prefix", "/tts");
        serverConfig.put("sjs_config", sjsConfig);

        container.deployModule("io.vertx~mod-web-server~2.0.0-final", new JsonObject(serverConfig));

        vertx.eventBus().registerHandler("tts.service", new TTSServiceHandler(this, timeout, latencyTimeout));
    }
}
