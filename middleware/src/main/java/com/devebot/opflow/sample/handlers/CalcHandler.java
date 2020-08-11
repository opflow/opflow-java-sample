package com.devebot.opflow.sample.handlers;

import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.exception.OpflowRequestTimeoutException;
import com.devebot.opflow.exception.OpflowServiceNotReadyException;
import com.devebot.opflow.exception.OpflowWorkerNotFoundException;
import com.devebot.opflow.sample.models.FibonacciInputItem;
import com.devebot.opflow.sample.models.FibonacciOutputItem;
import com.devebot.opflow.sample.services.FibonacciCalculator;
import com.devebot.opflow.supports.OpflowJsonTool;
import com.devebot.opflow.supports.OpflowObjectTree;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import java.text.MessageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author acegik
 */
public class CalcHandler implements HttpHandler {

    private final static Logger LOG = LoggerFactory.getLogger(CalcHandler.class);

    private final FibonacciCalculator calculator;

    public CalcHandler(FibonacciCalculator calculator) throws OpflowBootstrapException {
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
                    new Object[]{
                        data.getRequestId(), FibonacciInputItem.class.getCanonicalName(), data.getNumber()
                    }));
        }
        try {
            FibonacciOutputItem output = this.calculator.calc(data);
            System.out.println("[-] output: " + OpflowJsonTool.toString(output));
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(OpflowJsonTool.toString(output));
        } catch (OpflowServiceNotReadyException e) {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
            exchange.setStatusCode(530);
            exchange.getResponseSender().send(OpflowObjectTree.buildMap()
                    .put("reason", "suspend")
                    .put("message", e.getMessage())
                    .toString(true));
        } catch (OpflowRequestTimeoutException e) {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
            exchange.setStatusCode(598);
            exchange.getResponseSender().send(OpflowObjectTree.buildMap()
                    .put("reason", "timeout")
                    .put("message", e.getMessage())
                    .toString(true));
        } catch (OpflowWorkerNotFoundException e) {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
            exchange.setStatusCode(404);
            exchange.getResponseSender().send(OpflowObjectTree.buildMap()
                    .put("reason", "disabled")
                    .put("message", e.getMessage())
                    .toString(true));
        } catch (Exception exception) {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.setStatusCode(500);
            exchange.getResponseSender().send(exception.toString());
        }
    }
}
