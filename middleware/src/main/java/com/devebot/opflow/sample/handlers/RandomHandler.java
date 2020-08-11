package com.devebot.opflow.sample.handlers;

import com.devebot.opflow.OpflowUUID;
import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.sample.models.FibonacciInputItem;
import com.devebot.opflow.sample.models.FibonacciOutputItem;
import com.devebot.opflow.sample.services.FibonacciCalculator;
import com.devebot.opflow.sample.utils.CommonUtil;
import com.devebot.opflow.sample.utils.Randomizer;
import com.devebot.opflow.supports.OpflowJsonTool;
import com.devebot.opflow.supports.OpflowObjectTree;
import com.devebot.opflow.supports.OpflowStringUtil;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author acegik
 */
public class RandomHandler implements HttpHandler {

    private final FibonacciCalculator calculator;

    public RandomHandler(FibonacciCalculator calculator) throws OpflowBootstrapException {
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
                for (int i = 0; i < total; i++) {
                    final String requestId = reqId + "/" + OpflowStringUtil.pad(i, digit);
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
                    final int n = m;
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
                for (Future<Object> future : futures) {
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
