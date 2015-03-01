var TTSServiceHandler = function () {

    var eventBus;
    var userId;

    this.init = function(url) {
        eventBus = new vertx.EventBus(url);
        userId = chance.first() + "-" + chance.guid();
        return this;
    }

    this.getEventBus = function() {
        return eventBus;
    }

    this.getUserId = function() {
        return userId;
    }

    this.handle = function (reply) {

        if (reply.status === 200) {

            eventBus.registerHandler("tts.user." + userId, function (message) {
                clog("Got message from server with action: " + message.action);
                switch (message.action) {
                    case "ping":
                        eventBus.send("tts.service", { action: "ping", userId: userId});

                        break;

                }
            });

            document.getElementById("sendText").addEventListener("click", function () {
                var text = ttsText.value;
                eventBus.send("tts.send", { action: "submitText", userId: userId, text: text });
            }, false);
        }
    }
};