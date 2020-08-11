package com.devebot.opflow.sample;

import com.devebot.opflow.OpflowBuilder;
import com.devebot.opflow.OpflowCommander;
import com.devebot.opflow.OpflowConfig;
import com.devebot.opflow.OpflowConfigValidator;
import com.devebot.opflow.OpflowPromExporter;
import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.exception.OpflowConfigValidationException;
import com.devebot.opflow.exception.OpflowConnectionException;
import com.devebot.opflow.sample.handlers.AlertHandler;
import com.devebot.opflow.sample.handlers.SingleHandler;
import com.devebot.opflow.sample.handlers.RandomHandler;
import com.devebot.opflow.sample.services.AlertSender;
import com.devebot.opflow.sample.services.FibonacciCalculator;
import com.devebot.opflow.sample.services.FibonacciCalculatorImpl;
import com.devebot.opflow.supports.OpflowNetTool;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.PathTemplateHandler;
import java.util.Map;
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
    private final SingleHandler singleHandler;
    private final RandomHandler randomHandler;
    
    FibonacciMaster() throws OpflowBootstrapException {
        OpflowPromExporter.hook();
        FibonacciCalculator calcImpl = new FibonacciCalculatorImpl();
        
        OpflowConfig.Validator v1 = OpflowConfigValidator.getCommanderConfigValidator(
                        FibonacciMaster.class.getResourceAsStream("/master-schema.json"));
        
        OpflowConfig.Validator v2 = new OpflowConfig.Validator() {
            @Override
            public Object validate(Map<String, Object> configuration) throws OpflowConfigValidationException {
                // do something with configuration
                return null;
            }
        };
        
        this.commander = OpflowBuilder.createCommander("master.properties", v1, v2);
        
        this.alertSender = commander.registerTypeWithDefault(AlertSender.class);
        this.alertHandler = new AlertHandler(this.alertSender);
        this.calculator = commander.registerTypeWithDefault(FibonacciCalculator.class, calcImpl);
        this.clonedCalculator = commander.registerType("clonedCalc", FibonacciCalculator.class, calcImpl);
        this.sharedCalculator = commander.registerType("sharedCalc", FibonacciCalculator.class, calcImpl);
        this.singleHandler = new SingleHandler(this.calculator);
        this.randomHandler = new RandomHandler(this.calculator);
    }

    public PathTemplateHandler getPathTemplateHandler() {
        PathTemplateHandler ptHandler = Handlers.pathTemplate()
                .add("/alert", new BlockingHandler(this.alertHandler))
                .add("/fibonacci/{number}", this.singleHandler)
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
}
