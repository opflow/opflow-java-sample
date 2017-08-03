package com.devebot.opflow.example;

import java.util.Map;
import com.devebot.opflow.OpflowHelper;
import com.devebot.opflow.OpflowMessage;
import com.devebot.opflow.OpflowRpcMaster;
import com.devebot.opflow.OpflowRpcRequest;
import com.devebot.opflow.OpflowUtil;

public class FibonacciRpcMaster {

    public static void main(String[] argv) throws Exception {
        final OpflowRpcMaster rpc = OpflowHelper.createRpcMaster();
        
        System.out.println("[+] ExampleMaster request");

        OpflowRpcRequest result1 = rpc.request("fibonacci", OpflowUtil.buildJson(new OpflowUtil.MapListener() {
            @Override
            public void transform(Map<String, Object> opts) {
                opts.put("number", 20);
            }
        }), OpflowUtil.buildOptions(new OpflowUtil.MapListener() {
            @Override
            public void transform(Map<String, Object> opts) {
                opts.put("timeout", 5);
                //opts.put("mode", "standalone");
            }
        }));

        while(result1.hasNext()) {
            OpflowMessage msg = result1.next();
            System.out.println("[-] message1 received: " + msg.getContentAsString());
        }
        
        OpflowRpcRequest result2 = rpc.request("fib", OpflowUtil.buildJson(new OpflowUtil.MapListener() {
            @Override
            public void transform(Map<String, Object> opts) {
                opts.put("number", 30);
            }
        }), OpflowUtil.buildOptions(new OpflowUtil.MapListener() {
            @Override
            public void transform(Map<String, Object> opts) {
                opts.put("timeout", 10);
                opts.put("mode", "standalone");
            }
        }));
        
        while(result2.hasNext()) {
            OpflowMessage msg = result2.next();
            System.out.println("[-] message2 received: " + msg.getContentAsString());
        }
        
        System.out.println("[-] closing");
        rpc.close();
        
        System.out.println("[-] ExampleMaster has finished");
    }
}
