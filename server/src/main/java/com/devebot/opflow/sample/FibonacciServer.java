package com.devebot.opflow.sample;

import com.devebot.opflow.OpflowBuilder;
import com.devebot.opflow.OpflowServerlet;
import com.devebot.opflow.sample.services.AlertSenderImpl;
import com.devebot.opflow.sample.services.FibonacciCalculatorImpl;

/**
 *
 * @author drupalex
 */
public class FibonacciServer {
    public static void main(String[] argv) throws Exception {
        System.out.println("FibonacciServer start: ");
        final OpflowServerlet server = OpflowBuilder.createServerlet("server.properties");
        server.instantiateType(AlertSenderImpl.class);
        server.instantiateType(FibonacciCalculatorImpl.class);
        server.start();
        System.out.println("[*] Waiting for message. To exit press CTRL+C");
    }
}
