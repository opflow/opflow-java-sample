package com.devebot.opflow.example;

import java.io.IOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.devebot.opflow.OpflowHelper;
import com.devebot.opflow.OpflowMessage;
import com.devebot.opflow.OpflowPubsubHandler;
import com.devebot.opflow.OpflowPubsubListener;

/**
 *
 * @author drupalex
 */
public class OpflowPubsubExample {

    public static void main(String[] argv) throws Exception {
        final JsonParser jsonParser = new JsonParser();
        final OpflowPubsubHandler pubsub = OpflowHelper.createPubsubHandler();
        pubsub.subscribe(new OpflowPubsubListener() {
            @Override
            public void processMessage(OpflowMessage message) throws IOException {
                JsonObject jsonObject = (JsonObject)jsonParser.parse(new String(message.getContent(), "UTF-8"));
                System.out.println(" [+] Received '" + jsonObject.toString() + "'");
            }
        });
    }
}
