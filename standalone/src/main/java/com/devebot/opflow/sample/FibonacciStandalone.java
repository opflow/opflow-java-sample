package com.devebot.opflow.sample;

import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.exception.OpflowConnectionException;
import com.devebot.opflow.sample.handlers.SingleHandler;
import com.devebot.opflow.sample.handlers.RandomHandler;
import com.devebot.opflow.sample.services.FibonacciCalculator;
import com.devebot.opflow.sample.services.FibonacciCalculatorImpl;
import com.devebot.opflow.supports.OpflowNetTool;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.PathTemplateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author drupalex
 */
public class FibonacciStandalone {
    
    private final static Logger LOG = LoggerFactory.getLogger(FibonacciStandalone.class);
    
    private final FibonacciCalculator calculator;
    
    FibonacciStandalone() throws OpflowBootstrapException {
        this.calculator = new FibonacciCalculatorImpl();
    }

    public PathTemplateHandler getPathTemplateHandler() throws OpflowBootstrapException {
        PathTemplateHandler ptHandler = Handlers.pathTemplate()
                .add("/fibonacci/{number}", new SingleHandler(this.calculator))
                .add("/random/{total}", new BlockingHandler(new RandomHandler(this.calculator)));
        return ptHandler;
    }

    public void serve() {
    }

    public void close() throws Exception {
    }

    public static void main(String[] argv) throws Exception {
        try {
            final Integer port = OpflowNetTool.detectFreePort(8888, 8899);
            if (port == null) {
                System.err.println("[*] There is no free port in the range 8888 - 8899!");
                System.exit(-1);
            }
            final FibonacciStandalone master = new FibonacciStandalone();
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
}
