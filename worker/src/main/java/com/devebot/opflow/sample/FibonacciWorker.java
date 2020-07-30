package com.devebot.opflow.sample;

import com.devebot.opflow.OpflowBuilder;
import com.devebot.opflow.OpflowConfig;
import com.devebot.opflow.OpflowServerlet;
import com.devebot.opflow.exception.OpflowConfigValidationException;
import com.devebot.opflow.exception.OpflowConnectionException;
import com.devebot.opflow.sample.services.AlertSenderImpl;
import com.devebot.opflow.sample.services.FibonacciCalculatorImpl;
import com.devebot.opflow.supports.OpflowJsonTool;
import java.util.Map;

/**
 *
 * @author drupalex
 */
public class FibonacciWorker {
    public static void main(String[] argv) throws Exception {
        try {
            System.out.println("[+] FibonacciWorker start:");
            final OpflowServerlet serverlet = OpflowBuilder.createServerlet("worker.properties", new OpflowConfig.Validator() {
                @Override
                public Object validate(Map<String, Object> configuration) throws OpflowConfigValidationException {
                    System.out.println(OpflowJsonTool.toString(configuration, true));
                    return null;
                }
            });
            serverlet.instantiateType(AlertSenderImpl.class);
            serverlet.instantiateType(FibonacciCalculatorImpl.class);
            serverlet.serve();
            System.out.println("[*] Waiting for message. To exit press CTRL+C");
        }
        catch (OpflowConnectionException e) {
            System.err.println("[*] Invalid connection parameters or the RabbitMQ Server not available");
            Runtime.getRuntime().exit(1);
        }
    }
}
