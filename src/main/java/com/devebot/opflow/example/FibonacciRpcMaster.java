package com.devebot.opflow.example;

import com.devebot.opflow.OpflowHelper;
import com.devebot.opflow.OpflowMessage;
import com.devebot.opflow.OpflowRpcMaster;
import com.devebot.opflow.OpflowRpcRequest;
import com.devebot.opflow.OpflowRpcResult;
import com.devebot.opflow.OpflowUtil;
import com.devebot.opflow.exception.OpflowConstructorException;
import java.util.Map;

public class FibonacciRpcMaster {

    private final OpflowRpcMaster master;
    
    public FibonacciRpcMaster() throws OpflowConstructorException {
        master = OpflowHelper.createRpcMaster();
    }
    
    public OpflowRpcRequest request(final int number) {
        return request(number, 10000);
    }
    
    public OpflowRpcRequest request(final int number, final long timeout) {
        return master.request("fibonacci", OpflowUtil.buildJson(new OpflowUtil.MapListener() {
            @Override
            public void transform(Map<String, Object> opts) {
                opts.put("number", number);
            }
        }), OpflowUtil.buildOptions(new OpflowUtil.MapListener() {
            @Override
            public void transform(Map<String, Object> opts) {
                opts.put("timeout", timeout);
            }
        }));
    }
    
    public static void main(String[] argv) throws Exception {
        final FibonacciRpcMaster rpc = new FibonacciRpcMaster();
        
        System.out.println("[+] ExampleMaster request");

        OpflowRpcRequest req1 = rpc.request(20);
        OpflowRpcRequest req2 = rpc.request(30);
        
        // OpflowRpcRequest object works as an Interator<OpflowMessage>
        while(req1.hasNext()) {
            OpflowMessage msg = req1.next();
            System.out.println("[-] message1 received: " + msg.getContentAsString() + 
                    " / workerTag: " + msg.getInfo().get("workerTag"));
        }
        System.out.println();
        
        // Transforms OpflowRpcRequest object to OpflowRpcResult object
        OpflowRpcResult result2 = OpflowUtil.exhaustRequest(req2);
        System.out.println("[-] message2 worker: " + result2.getWorkerTag());
        for(OpflowRpcResult.Step step: result2.getProgress()) {
            System.out.println("[-] message2 percent: " + step.getPercent());
        }
        System.out.println("[-] message2 result: " + result2.getValueAsString());
        
        System.out.println("[-] ExampleMaster has finished");
    }
}
