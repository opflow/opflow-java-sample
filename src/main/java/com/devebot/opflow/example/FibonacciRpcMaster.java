package com.devebot.opflow.example;

import com.devebot.opflow.OpflowMessage;
import com.devebot.opflow.OpflowRpcRequest;

public class FibonacciRpcMaster {

    public static void main(String[] argv) throws Exception {
        final FibonacciRpcImpl rpc = new FibonacciRpcImpl();
        
        System.out.println("[+] ExampleMaster request");

        OpflowRpcRequest result1 = rpc.request(20);
        OpflowRpcRequest result2 = rpc.request(30);
        
        while(result1.hasNext()) {
            OpflowMessage msg = result1.next();
            System.out.println("[-] message1 received: " + msg.getContentAsString());
        }
        
        while(result2.hasNext()) {
            OpflowMessage msg = result2.next();
            System.out.println("[-] message2 received: " + msg.getContentAsString());
        }
        
        System.out.println("[-] ExampleMaster has finished");
    }
}
