package com.devebot.opflow.sample;

import com.devebot.opflow.OpflowBuilder;
import com.devebot.opflow.OpflowCommander;
import com.devebot.opflow.OpflowJsontool;
import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.sample.models.FibonacciOutput;
import com.devebot.opflow.sample.services.FibonacciCalculator;
import com.devebot.opflow.sample.services.FibonacciCalculatorImpl;
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
public class FibonacciClient {
    public static void main(String[] argv) throws Exception {
        FibonacciApi api = new FibonacciApi();
        Undertow server = Undertow.builder()
                .addHttpListener(8989, "0.0.0.0")
                .setHandler(Handlers.pathTemplate().add("/fibonacci/{number}", new CalcHandler(api)))
                .build();
        server.start();
    }

    static class CalcHandler implements HttpHandler {
        
        private final FibonacciApi api;

        CalcHandler(FibonacciApi api) throws OpflowBootstrapException {
            this.api = api;
        }
        
        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

            // get the number
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String number = pathMatch.getParameters().get("number");
            System.out.println("[+] Make a RPC call with number: " + number);
            try {
                FibonacciOutput output = this.api.calculator.calc(Integer.parseInt(number));
                System.out.println("[-] output: " + OpflowJsontool.toString(output));
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(OpflowJsontool.toString(output));
            } catch (Exception exception) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.getResponseSender().send(exception.toString());
            }
        }
    }
    
    static class FibonacciApi implements AutoCloseable {
        private final OpflowCommander commander;
        private final FibonacciCalculator calculator;
        
        FibonacciApi() throws OpflowBootstrapException {
            this.commander = OpflowBuilder.createCommander("client.properties");
            this.calculator = commander.registerType(FibonacciCalculator.class, new FibonacciCalculatorImpl());
            System.out.println("[+> ping-pong result: " + this.commander.ping());
        }
        
        @Override
        public void close() throws Exception {
            commander.close();
        }
    }
}
