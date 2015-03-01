package com.texttalk.frontend;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Created by andrew on 28/02/15.
 */
public class TTSServiceHandler implements Handler<Message<JsonObject>> {

    public final String PREFIX = "tts.";

    Logger logger = null;
    private Verticle verticle;
    private Vertx vertx;
    private Long timeout;
    private Long latencyTimeout;

    private final ConcurrentMap<String, TTSUser> users;
    private final ConcurrentMap<String, Long> timers;

    TTSServiceHandler(Verticle verticle, Long timeout, long latencyTimeout) {

        this.verticle = verticle;
        vertx = verticle.getVertx();
        this.timeout = timeout;
        this.latencyTimeout = latencyTimeout;

        users = vertx.sharedData().getMap("tts.users");
        timers = vertx.sharedData().getMap("tts.timers");
        logger = verticle.getContainer().logger();
    }

    @Override
    public void handle(Message<JsonObject> event) {

        JsonObject message = event.body();
        String userId = message.getString("userId");
        JsonObject reply = new JsonObject().putBoolean("success", true);

        Consumer<String> errorReply = (String msg) -> reply.putBoolean("success", false).putString("error", msg);

        switch(message.getString("action")) {

            case "connect":

                logger.info("tts.connect: " + message);

                timers.put(userId, createTimer(userId, timeout));
                reply.putNumber("status", 200);
                reply.putString("message", "You are connected!");

                break;

            case "ping":

                if (resetTimer(userId)) {
                    reply.putNumber("pong", timeout);
                } else {
                    errorReply.accept("Could not reset timer (too late, maybe?).");
                }

                break;


        }

        event.reply(reply);

    }

    public Long createTimer(final String userId, Long timeout) {
        return verticle.getVertx().setTimer(timeout, new Handler<Long>() {
            @Override
            public void handle(Long timerId) {
                vertx.eventBus().send("tts.user." + userId, new JsonObject().putString("action", "ping"));
                Long newTimerId = vertx.setTimer(latencyTimeout, new Handler<Long>() {
                    @Override
                    public void handle(Long timerId) {
                        vertx.eventBus().send("tts.user." + userId, new JsonObject().putString("action", "disconnect"));
                        disconnectUser(userId);
                    }
                });
                if (timers.replace(userId, timerId, newTimerId)) {
                    vertx.cancelTimer(timerId);
                }
            }
        });
    }

    private boolean resetTimer(String userId) {
        if(vertx.cancelTimer(timers.get(userId))) {
            timers.put(userId, createTimer(userId, timeout));
            return true;
        } else {
            return false;
        }
    }

    private void disconnectUser(String userId) {
        users.remove(userId);
        vertx.cancelTimer(timers.remove(userId));
        logger.info("Disconnected user: " + userId);
    }

    private void sendMessage(String userId, String action, JsonObject msg) {

        msg.putString("action", action);
        vertx.eventBus().send("tts.user." + userId, msg);
    }
}
