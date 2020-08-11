package com.devebot.opflow.sample.handlers;

import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.sample.models.AlertMessage;
import com.devebot.opflow.sample.services.AlertSender;
import com.devebot.opflow.supports.OpflowJsonTool;
import com.devebot.opflow.supports.OpflowObjectTree;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 *
 * @author acegik
 */
public class AlertHandler implements HttpHandler {

    private final AlertSender alertSender;

    public AlertHandler(AlertSender sender) throws OpflowBootstrapException {
        this.alertSender = sender;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            System.out.println("[+] Alert");
            AlertMessage message = OpflowJsonTool.toObject(exchange.getInputStream(), AlertMessage.class);
            System.out.println("[-] message: " + OpflowJsonTool.toString(message));
            this.alertSender.notify(message);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(OpflowJsonTool.toString(message));
        } catch (Exception exception) {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.setStatusCode(500);
            exchange.getResponseSender().send(OpflowObjectTree.buildMap()
                    .put("name", exception.getClass().getName())
                    .put("message", exception.getMessage())
                    .toString(true));
        }
    }
}
