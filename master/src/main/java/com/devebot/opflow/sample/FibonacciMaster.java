package com.devebot.opflow.sample;

import com.devebot.opflow.OpflowBuilder;
import com.devebot.opflow.OpflowCommander;
import com.devebot.opflow.OpflowConfig;
import com.devebot.opflow.OpflowConfigValidator;
import com.devebot.opflow.OpflowPromExporter;
import com.devebot.opflow.OpflowUUID;
import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.exception.OpflowConnectionException;
import com.devebot.opflow.exception.OpflowServiceNotReadyException;
import com.devebot.opflow.exception.OpflowRequestTimeoutException;
import com.devebot.opflow.exception.OpflowWorkerNotFoundException;
import com.devebot.opflow.sample.models.AlertMessage;
import com.devebot.opflow.sample.models.FibonacciInputItem;
import com.devebot.opflow.sample.models.FibonacciOutputItem;
import com.devebot.opflow.sample.services.AlertSender;
import com.devebot.opflow.sample.services.FibonacciCalculator;
import com.devebot.opflow.sample.services.FibonacciCalculatorImpl;
import com.devebot.opflow.sample.utils.CommonUtil;
import com.devebot.opflow.sample.utils.Randomizer;
import com.devebot.opflow.supports.OpflowJsonTool;
import com.devebot.opflow.supports.OpflowNetTool;
import com.devebot.opflow.supports.OpflowObjectTree;
import com.devebot.opflow.supports.OpflowStringUtil;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final FibonacciCalculator clonedCalculator;
    private final FibonacciCalculator sharedCalculator;
    private final CalcHandler calcHandler;
    private final RandomHandler randomHandler;
    private final OpflowConfig.Validator validator = OpflowConfigValidator
            .getCommanderConfigValidator(FibonacciMaster.class.getResourceAsStream("/master-schema.json"));
    
    FibonacciMaster() throws OpflowBootstrapException {
        OpflowPromExporter.hook();
        FibonacciCalculator calcImpl = new FibonacciCalculatorImpl();
        this.commander = OpflowBuilder.createCommander("master.properties", validator);
        this.alertSender = commander.registerTypeWithDefault(AlertSender.class);
        this.alertHandler = new AlertHandler(this.alertSender);
        this.calculator = commander.registerTypeWithDefault(FibonacciCalculator.class, calcImpl);
        this.clonedCalculator = commander.registerType("clonedCalc", FibonacciCalculator.class, calcImpl);
        this.sharedCalculator = commander.registerType("sharedCalc", FibonacciCalculator.class, calcImpl);
        this.calcHandler = new CalcHandler(this.calculator);
        this.randomHandler = new RandomHandler(this.calculator);
    }

    public PathTemplateHandler getPathTemplateHandler() {
        PathTemplateHandler ptHandler = Handlers.pathTemplate()
                .add("/alert", new BlockingHandler(this.alertHandler))
                .add("/fibonacci/{number}", this.calcHandler)
                .add("/random/{total}", new BlockingHandler(this.randomHandler));
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
            final Integer port = OpflowNetTool.detectFreePort(8888, 8899);
            if (port == null) {
                System.err.println("[*] There is no free port in the range 8888 - 8899!");
                System.exit(-1);
            }
            final FibonacciMaster master = new FibonacciMaster();
                    final GracefulShutdownHandler shutdownHander = new GracefulShutdownHandler(master.getPathTemplateHandler());
            final Undertow server = Undertow.builder()
                    .addHttpListener(port, "0.0.0.0")
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
            System.out.println("[*] Listening for HTTP on 0.0.0.0:" + port);
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
                exchange.getResponseSender().send(OpflowObjectTree.buildMap()
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
                LOG.debug(MessageFormat.format("Fibonacci[{0}] - calc({1}) with number: {2}", 
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
            catch (OpflowServiceNotReadyException e) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
                exchange.setStatusCode(530);
                exchange.getResponseSender().send(OpflowObjectTree.buildMap()
                        .put("reason", "suspend")
                        .put("message", e.getMessage())
                        .toString(true));
            }
            catch (OpflowRequestTimeoutException e) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
                exchange.setStatusCode(598);
                exchange.getResponseSender().send(OpflowObjectTree.buildMap()
                        .put("reason", "timeout")
                        .put("message", e.getMessage())
                        .toString(true));
            }
            catch (OpflowWorkerNotFoundException e) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
                exchange.setStatusCode(404);
                exchange.getResponseSender().send(OpflowObjectTree.buildMap()
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
            ExecutorService executor = null;
            // get the requestId
            HeaderMap headers = exchange.getRequestHeaders();
            String reqId = headers.getFirst("X-Request-Id");
            if (reqId == null) {
                reqId = OpflowUUID.getBase64ID();
            }
            try {
                PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
                String totalStr = pathMatch.getParameters().get("total");
                System.out.println("[+] Make a RPC call with number: " + totalStr);
                Integer total = Integer.parseInt(totalStr);
                
                RandomOptions opts = null;
                String method = exchange.getRequestMethod().toString();
                switch (method) {
                    case "PUT":
                        opts = OpflowJsonTool.toObject(exchange.getInputStream(), RandomOptions.class);
                        break;
                }
                if (opts == null) {
                    opts = new RandomOptions();
                }

                if (opts.getExceptionTotal() > total) {
                    throw new IllegalArgumentException("exceptionTotal[" + opts.getExceptionTotal() + "] is greater than total[" + total + "]");
                }

                executor = Executors.newFixedThreadPool(opts.getConcurrentCalls());

                final RandomResult store = new RandomResult();
                if (total > 0) {
                    List<Callable<Object>> tasks = new ArrayList<>();
                    int exceptionCount = 0;
                    int digit = CommonUtil.countDigit(total);
                    for (int i = 0; i<total; i++) {
                        String requestId = reqId + "/" + OpflowStringUtil.pad(i, digit);
                        int m = Randomizer.random(2, 45);
                        int remains = opts.getExceptionTotal() - exceptionCount;
                        if (0 < remains) {
                            if (remains < (total - i)) {
                                if (m % 2 != 0) {
                                    m = 100;
                                    exceptionCount++;
                                }
                            } else {
                                m = 100;
                                exceptionCount++;
                            }
                        }
                        int n = m;
                        tasks.add(new Callable() {
                            @Override
                            public Object call() throws Exception {
                                store.total.incrementAndGet();
                                try {
                                    switch (n % 3) {
                                        case 0:
                                            return calculator.calc(n);
                                        case 1:
                                            return calculator.calc(new FibonacciInputItem(n, requestId));
                                        default:
                                            ArrayList<FibonacciInputItem> list = new ArrayList<>();
                                            list.add(new FibonacciInputItem(n, requestId));
                                            return calculator.calc(list);
                                    }
                                } catch (Exception e) {
                                    store.errorsTotal.incrementAndGet();
                                    return OpflowObjectTree.buildMap()
                                            .put("number", n)
                                            .put("errorClass", e.getClass().getName())
                                            .put("errorMessage", e.getMessage())
                                            .toMap();
                                }
                            }
                        });
                    }
                    List<Future<Object>> futures = executor.invokeAll(tasks);
                    for (Future<Object> future: futures) {
                        Object result = future.get();
                        if (opts.isReturnErrorOnly()) {
                            if (!(result instanceof FibonacciOutputItem)) {
                                store.output.add(result);
                            }
                        } else {
                            store.output.add(result);
                        }
                    }
                }
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(OpflowJsonTool.toString(store, true));
            } catch (Exception exception) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.setStatusCode(500);
                exchange.getResponseSender().send(exception.toString());
            } finally {
                if (executor != null) {
                    executor.shutdown();
                    executor.awaitTermination(1, TimeUnit.SECONDS);
                }
            }            
        }
    }
    
    static class RandomResult {
        protected AtomicInteger total = new AtomicInteger(0);
        protected AtomicInteger errorsTotal = new AtomicInteger(0);
        List<Object> output = new ArrayList<>();
    }
    
    static class RandomOptions {
        private int concurrentCalls = 100;
        private int exceptionTotal = 0;
        private boolean returnErrorOnly = false;

        public RandomOptions() {
        }

        public RandomOptions(int concurrentCalls, int exceptionTotal) {
            if (concurrentCalls > 0) {
                this.concurrentCalls = concurrentCalls;
            }
            this.exceptionTotal = exceptionTotal;
        }

        public int getConcurrentCalls() {
            return concurrentCalls;
        }

        public int getExceptionTotal() {
            return exceptionTotal;
        }

        public boolean isReturnErrorOnly() {
            return returnErrorOnly;
        }
    }
}
