package com.devebot.opflow.sample;

import com.devebot.opflow.OpflowBuilder;
import com.devebot.opflow.OpflowCommander;
import com.devebot.opflow.OpflowUtil;
import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.exception.OpflowConnectionException;
import com.devebot.opflow.exception.OpflowRequestSuspendException;
import com.devebot.opflow.exception.OpflowRequestTimeoutException;
import com.devebot.opflow.exception.OpflowWorkerNotFoundException;
import com.devebot.opflow.sample.models.AlertMessage;
import com.devebot.opflow.sample.models.FibonacciInputItem;
import com.devebot.opflow.sample.models.FibonacciOutputItem;
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
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.PathTemplateHandler;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author drupalex
 */
public class FibonacciMaster implements AutoCloseable {
    
    private final static Logger LOG = LoggerFactory.getLogger(FibonacciMaster.class);
    
    private final OpflowCommander commander;
    private final AlertSender alertSender;
    private final AlertHandler alertHandler;
    private final FibonacciCalculator calculator;
    private final CalcHandler calcHandler;
    private final RandomHandler randomHandler;

    FibonacciMaster() throws OpflowBootstrapException {
        this.commander = OpflowBuilder.createCommander("master.properties");
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
    
    public static void main(String[] argv) throws Exception {
        try {
            final FibonacciMaster master = new FibonacciMaster();
                    final GracefulShutdownHandler shutdownHander = new GracefulShutdownHandler(master.getPathTemplateHandler());
            final Undertow server = Undertow.builder()
                    .addHttpListener(8888, "0.0.0.0")
                    .setHandler(shutdownHander)
                    .build();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.out.println("[-] The server is shutting down ...");
                    try {
                        shutdownHander.shutdown();
                        shutdownHander.awaitShutdown(1000);
                        System.out.println("[-] shutdownHander has been done");
                        master.close();
                        System.out.println("[-] Commander has been stopped");
                        server.stop();
                        System.out.println("[-] Webserver has been stopped");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            master.serve();
            server.start();
            System.out.println("[*] Listening for HTTP on 0.0.0.0:8888");
        }
        catch (OpflowConnectionException e) {
            System.err.println("[*] Invalid connection parameters or the RabbitMQ Server not available");
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
            // get the requestId
            HeaderMap headers = exchange.getRequestHeaders();
            String requestId = headers.getFirst("X-Request-Id");
            // get the number
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String number = pathMatch.getParameters().get("number");
            FibonacciInputItem data = new FibonacciInputItem(Integer.parseInt(number), requestId);
            
            System.out.println("[+] Make a RPC call with number: " + number + " with requestId: " + requestId);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("Request[{0}] - calc({1}) with number: {2}", 
                        new Object[] {
                            data.getRequestId(), FibonacciInputItem.class.getCanonicalName(), data.getNumber()
                        }));
            }
            try {
                FibonacciOutputItem output = this.calculator.calc(data);
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
            catch (OpflowWorkerNotFoundException e) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
                exchange.setStatusCode(404);
                exchange.getResponseSender().send(OpflowUtil.buildOrderedMap()
                        .put("reason", "disabled")
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
                                list.add(this.calculator.calc(new FibonacciInputItem(n)));
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
