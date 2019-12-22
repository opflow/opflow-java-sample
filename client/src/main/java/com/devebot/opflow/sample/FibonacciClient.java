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
import io.undertow.server.handlers.PathTemplateHandler;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import java.util.Map;

/**
 *
 * @author drupalex
 */
public class FibonacciClient {
    public static void main(String[] argv) throws Exception {
        FibonacciApi api = new FibonacciApi();
        Undertow server = Undertow.builder()
                .addHttpListener(8989, "0.0.0.0")
                .setHandler(api.getPathTemplateHandler())
                .build();
        server.start();
    }

    static class FibonacciApi implements AutoCloseable {
        private final OpflowCommander commander;
        private final FibonacciCalculator calculator;
        private final CalcHandler calcHandler;
        
        FibonacciApi() throws OpflowBootstrapException {
            this.commander = OpflowBuilder.createCommander("client.properties");
            this.calculator = commander.registerType(FibonacciCalculator.class, new FibonacciCalculatorImpl());
            this.calcHandler = new CalcHandler(this.calculator);
        }
        
        public PathTemplateHandler getPathTemplateHandler() {
            PathTemplateHandler ptHandler = Handlers.pathTemplate()
                    .add("/fibonacci/{number}", this.calcHandler);
            for(Map.Entry<String, HttpHandler> entry:commander.getInfoHttpHandlers().entrySet()) {
                ptHandler.add(entry.getKey(), entry.getValue());
            }
            return ptHandler;
        }
        
        @Override
        public void close() throws Exception {
            commander.close();
        }
    }
    
    static class CalcHandler implements HttpHandler {
        
        private final FibonacciCalculator calculator;

        CalcHandler(FibonacciCalculator calculator) throws OpflowBootstrapException {
            this.calculator = calculator;
        }
        
        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            // get the number
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String number = pathMatch.getParameters().get("number");
            System.out.println("[+] Make a RPC call with number: " + number);
            try {
                FibonacciOutput output = this.calculator.calc(Integer.parseInt(number));
                System.out.println("[-] output: " + OpflowJsontool.toString(output));
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(OpflowJsontool.toString(output));
            } catch (NumberFormatException exception) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.setStatusCode(500);
                exchange.getResponseSender().send(exception.toString());
            }
        }
    }
}
