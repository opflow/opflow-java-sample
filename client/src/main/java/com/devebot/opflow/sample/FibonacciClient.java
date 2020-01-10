package com.devebot.opflow.sample;

import com.devebot.opflow.OpflowBuilder;
import com.devebot.opflow.OpflowCommander;
import com.devebot.opflow.OpflowUtil;
import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.exception.OpflowRequestSuspendException;
import com.devebot.opflow.exception.OpflowRequestTimeoutException;
import com.devebot.opflow.sample.models.AlertMessage;
import com.devebot.opflow.sample.models.FibonacciInput;
import com.devebot.opflow.sample.models.FibonacciOutput;
import com.devebot.opflow.sample.services.AlertSender;
import com.devebot.opflow.sample.services.FibonacciCalculator;
import com.devebot.opflow.sample.services.FibonacciCalculatorImpl;
import com.devebot.opflow.sample.utils.Randomizer;
import com.devebot.opflow.supports.OpflowJsonTool;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.PathTemplateHandler;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author drupalex
 */
public class FibonacciClient {
    
    public static void main(String[] argv) throws Exception {
        FibonacciApi api = new FibonacciApi();
        api.serve();
        Undertow server = Undertow.builder()
                .addHttpListener(8888, "0.0.0.0")
                .setHandler(api.getPathTemplateHandler())
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    server.stop();
                    api.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        server.start();
    }
    
    static class FibonacciApi implements AutoCloseable {
        private final OpflowCommander commander;
        private final AlertSender alertSender;
        private final AlertHandler alertHandler;
        private final FibonacciCalculator calculator;
        private final CalcHandler calcHandler;
        private final RandomHandler randomHandler;
        
        FibonacciApi() throws OpflowBootstrapException {
            this.commander = OpflowBuilder.createCommander("client.properties");
            this.alertSender = commander.registerType(AlertSender.class);
            this.alertHandler = new AlertHandler(this.alertSender);
            this.calculator = commander.registerType(FibonacciCalculator.class, new FibonacciCalculatorImpl());
            this.calcHandler = new CalcHandler(this.calculator);
            this.randomHandler = new RandomHandler(this.calculator);
        }
        
        public PathTemplateHandler getPathTemplateHandler() {
            PathTemplateHandler ptHandler = Handlers.pathTemplate()
                    .add("/alert", new BlockingHandler(this.alertHandler))
                    .add("/fibonacci/{number}", this.calcHandler)
                    .add("/random/{total}", this.randomHandler);
            return ptHandler;
        }
        
        public void serve() {
            this.commander.serve();
        }
        
        @Override
        public void close() throws Exception {
            commander.close();
        }
    }
    
    static class AlertHandler implements HttpHandler {
        
        private final AlertSender alertSender;
        
        AlertHandler(AlertSender sender) throws OpflowBootstrapException {
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
                exchange.getResponseSender().send(OpflowUtil.buildOrderedMap()
                        .put("name", exception.getClass().getName())
                        .put("message", exception.getMessage())
                        .toString(true));
            }
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
                System.out.println("[-] output: " + OpflowJsonTool.toString(output));
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(OpflowJsonTool.toString(output));
            }
            catch (OpflowRequestSuspendException e) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
                exchange.setStatusCode(530);
                exchange.getResponseSender().send(OpflowUtil.buildOrderedMap()
                        .put("reason", "suspend")
                        .put("message", e.getMessage())
                        .toString(true));
            }
            catch (OpflowRequestTimeoutException e) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
                exchange.setStatusCode(598);
                exchange.getResponseSender().send(OpflowUtil.buildOrderedMap()
                        .put("reason", "timeout")
                        .put("message", e.getMessage())
                        .toString(true));
            }
            catch (Exception exception) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.setStatusCode(500);
                exchange.getResponseSender().send(exception.toString());
            }
        }
    }
    
    static class RandomHandler implements HttpHandler {
        
        private final FibonacciCalculator calculator;

        RandomHandler(FibonacciCalculator calculator) throws OpflowBootstrapException {
            this.calculator = calculator;
        }
        
        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            // get the number
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String totalStr = pathMatch.getParameters().get("total");
            System.out.println("[+] Make a RPC call with number: " + totalStr);
            try {
                List<Object> list = new ArrayList<>();
                Integer total = Integer.parseInt(totalStr);
                if (total > 0) {
                    for (int i = 0; i<total; i++) {
                        int n = Randomizer.random(2, 45);
                        try {
                            if (n % 2 == 0) {
                                list.add(this.calculator.calc(n));
                            } else {
                                list.add(this.calculator.calc(new FibonacciInput(n)));
                            }
                        } catch (Exception e) {
                            list.add(OpflowUtil.buildOrderedMap()
                                    .put("number", n)
                                    .put("errorClass", e.getClass().getName())
                                    .put("errorMessage", e.getMessage())
                                    .toMap());
                        }
                    }
                }
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(OpflowJsonTool.toString(list, true));
            } catch (NumberFormatException exception) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.setStatusCode(500);
                exchange.getResponseSender().send(exception.toString());
            }
        }
    }
}
