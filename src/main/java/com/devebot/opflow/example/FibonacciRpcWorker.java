package com.devebot.opflow.example;

import com.devebot.opflow.OpflowBroker;
import com.devebot.opflow.OpflowHelper;
import com.devebot.opflow.OpflowMessage;
import com.devebot.opflow.OpflowRpcListener;
import com.devebot.opflow.OpflowRpcResponse;
import com.devebot.opflow.OpflowRpcWorker;
import com.devebot.opflow.exception.OpflowConstructorException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FibonacciRpcWorker {

    private final static Logger LOG = LoggerFactory.getLogger(FibonacciRpcWorker.class);
    private final Gson gson = new Gson();
    private final JsonParser jsonParser = new JsonParser();
    private final OpflowRpcWorker worker;
    
    public FibonacciRpcWorker() throws OpflowConstructorException {
        worker = OpflowHelper.createRpcWorker();
    }
    
    public OpflowBroker.ConsumerInfo process() {
        OpflowBroker.ConsumerInfo info = worker.process(new OpflowRpcListener() {
            @Override
            public Boolean processMessage(OpflowMessage message, OpflowRpcResponse response) throws IOException {
                LOG.debug("[+] Routine input: " + message.getContentAsString());
                return OpflowRpcListener.NEXT;
            }
        });
        
        worker.process(new String[] {"fibonacci", "fib"}, new OpflowRpcListener() {
            @Override
            public Boolean processMessage(OpflowMessage message, OpflowRpcResponse response) throws IOException {
                JsonObject jsonObject = (JsonObject)jsonParser.parse(message.getContentAsString());
                LOG.debug("[+] Fibonacci received: '" + jsonObject.toString() + "'");

                response.emitStarted();

                int number = Integer.parseInt(jsonObject.get("number").toString());
                FibonacciGenerator fibonacci = new FibonacciGenerator(number);

                while(fibonacci.next()) {
                    FibonacciGenerator.Result r = fibonacci.result();
                    response.emitProgress(r.getStep(), r.getNumber(), null);
                }

                String result = gson.toJson(fibonacci.result());
                LOG.debug("[-] Fibonacci finished with: '" + result + "'");

                response.emitCompleted(result);
                
                return null;
            }
        });
        
        return info;
    }
    
    public static void main(String[] argv) throws Exception {
        final FibonacciRpcWorker rpc = new FibonacciRpcWorker();
        rpc.process();
    }
}
