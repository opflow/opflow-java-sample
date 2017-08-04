package com.devebot.opflow.example;

public class FibonacciRpcWorker {

    public static void main(String[] argv) throws Exception {
        final FibonacciRpcImpl rpc = new FibonacciRpcImpl();
        rpc.process();
    }
}
