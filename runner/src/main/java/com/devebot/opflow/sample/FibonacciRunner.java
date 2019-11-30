package com.devebot.opflow.sample;

import com.devebot.opflow.OpflowBuilder;
import com.devebot.opflow.OpflowCommander;
import com.devebot.opflow.OpflowJsontool;
import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.sample.models.FibonacciOutput;
import com.devebot.opflow.sample.services.FibonacciCalculator;
import io.undertow.Handlers;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;

/**
 *
 * @author drupalex
 */
public class FibonacciRunner {
    public static void main(String[] argv) throws Exception {
        Undertow server = Undertow.builder()
                .addHttpListener(8989, "0.0.0.0")
                .setHandler(Handlers.pathTemplate().add("/fibonacci/{number}", new ItemHandler()))
                .build();
        server.start();
    }

    static class ItemHandler implements HttpHandler, AutoCloseable {
        
        private final OpflowCommander commander;
        private final FibonacciCalculator fib;

        ItemHandler() throws OpflowBootstrapException {
            this.commander = OpflowBuilder.createCommander("client.properties");
            this.fib = commander.registerType(FibonacciCalculator.class);
        }
        
        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

            // get the number
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String number = pathMatch.getParameters().get("number");
            System.out.println("[+] Make a RPC call with number: " + number);
            try {
                FibonacciOutput output = fib.calc(Integer.parseInt(number));
                System.out.println("[-] output: " + OpflowJsontool.toString(output));
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(OpflowJsontool.toString(output));
            } catch (Exception exception) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.getResponseSender().send(exception.toString());
            }
            // Method 2
//            String itemId2 = exchange.getQueryParameters().get("number").getFirst();
        }

        @Override
        public void close() throws Exception {
            commander.close();
        }
    }
}
