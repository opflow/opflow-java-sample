package com.devebot.opflow.example;

import com.devebot.opflow.OpflowBroker.ConsumerInfo;
import java.util.Map;
import com.devebot.opflow.OpflowHelper;
import com.devebot.opflow.OpflowMessage;
import com.devebot.opflow.OpflowRpcListener;
import com.devebot.opflow.OpflowRpcMaster;
import com.devebot.opflow.OpflowRpcRequest;
import com.devebot.opflow.OpflowRpcResponse;
import com.devebot.opflow.OpflowRpcWorker;
import com.devebot.opflow.OpflowUtil;
import com.devebot.opflow.exception.OpflowConstructorException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;

/**
 *
 * @author drupalex
 */
public class FibonacciRpcImpl {
    
    private final Gson gson = new Gson();
    private final JsonParser jsonParser = new JsonParser();
    private final OpflowRpcMaster master;
    private final OpflowRpcWorker worker;
    
    public FibonacciRpcImpl() throws OpflowConstructorException {
        master = OpflowHelper.createRpcMaster();
        worker = OpflowHelper.createRpcWorker();
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
    
    public ConsumerInfo process() {
        ConsumerInfo info = worker.process(new OpflowRpcListener() {
            @Override
            public Boolean processMessage(OpflowMessage message, OpflowRpcResponse response) throws IOException {
                System.out.println("[+] Routine input: " + message.getContentAsString());
                return OpflowRpcListener.NEXT;
            }
        });
        
        worker.process(new String[] {"fibonacci", "fib"}, new OpflowRpcListener() {
            @Override
            public Boolean processMessage(OpflowMessage message, OpflowRpcResponse response) throws IOException {
                JsonObject jsonObject = (JsonObject)jsonParser.parse(message.getContentAsString());
                System.out.println("[+] Fibonacci received: '" + jsonObject.toString() + "'");

                response.emitStarted();

                int number = Integer.parseInt(jsonObject.get("number").toString());
                FibonacciGenerator fibonacci = new FibonacciGenerator(number);

                while(fibonacci.next()) {
                    FibonacciGenerator.Result r = fibonacci.result();
                    response.emitProgress(r.getStep(), r.getNumber(), null);
                }

                String result = gson.toJson(fibonacci.result());
                System.out.println("[-] Fibonacci finished with: '" + result + "'");

                response.emitCompleted(result);
                
                return null;
            }
        });
        
        return info;
    }
}
