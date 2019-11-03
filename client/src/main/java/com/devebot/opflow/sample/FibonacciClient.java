package com.devebot.opflow.sample;

import com.devebot.opflow.OpflowBuilder;
import com.devebot.opflow.OpflowCommander;
import com.devebot.opflow.OpflowJsontool;
import com.devebot.opflow.sample.models.FibonacciOutput;
import com.devebot.opflow.sample.services.FibonacciCalculator;

/**
 *
 * @author drupalex
 */
public class FibonacciCommander {
    public static void main(String[] argv) throws Exception {
        System.out.println("FibonacciCommander start: ");
        final OpflowCommander commander = OpflowBuilder.createCommander("client.properties");
        final FibonacciCalculator fib = commander.registerType(FibonacciCalculator.class);
        if (true) {
            System.out.println("[+] Make a RPC call:");
            try {
                FibonacciOutput output = fib.calc(45);
                System.out.println("[-] output: " + OpflowJsontool.toString(output));
            } finally {
                System.out.println("[-] closing the commander");
                commander.close();
            }
        } else {
            Thread t = new Thread() {
                @Override
                public void run() {
                    System.out.println("[+] Make a RPC call:");
                    FibonacciOutput output1 = fib.calc(45);
                }
            };
            t.start();
            Thread.sleep(1000);
            System.out.println("[-] closing the commander");
            commander.close();
            System.out.println("[*] program will be stopped");
        }
    }
}
