package com.devebot.opflow.example;

import com.devebot.opflow.OpflowEngine;
import com.devebot.opflow.OpflowLoader;
import com.devebot.opflow.OpflowMessage;
import com.devebot.opflow.OpflowRpcListener;
import com.devebot.opflow.OpflowRpcResponse;
import com.devebot.opflow.OpflowRpcWorker;
import com.devebot.opflow.OpflowUtil;
import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.exception.OpflowOperationException;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FibonacciRpcWorker {

    private final static Logger LOG = LoggerFactory.getLogger(FibonacciRpcWorker.class);
    private final OpflowRpcWorker worker;
    private final FibonacciSetting setting;
    
    public FibonacciRpcWorker() throws OpflowBootstrapException {
        this(null);
    }
    
    public FibonacciRpcWorker(FibonacciSetting setting) throws OpflowBootstrapException {
        this.setting = (setting != null) ? setting : new FibonacciSetting();
        this.worker = OpflowLoader.createRpcWorker();
    }
    
    public OpflowEngine.ConsumerInfo process() {
        OpflowEngine.ConsumerInfo info = worker.process(new OpflowRpcListener() {
            @Override
            public Boolean processMessage(OpflowMessage message, OpflowRpcResponse response) throws IOException {
                LOG.debug("[+] Routine input: " + message.getBodyAsString());
                return OpflowRpcListener.NEXT;
            }
        });
        
        worker.process(new String[] {"fibonacci", "fib"}, new OpflowRpcListener() {
            @Override
            public Boolean processMessage(OpflowMessage message, OpflowRpcResponse response) throws IOException {
                try {
                    String msg = message.getBodyAsString();
                    LOG.debug("[+] Fibonacci received: '" + msg + "'");
                    
                    // OPTIONAL
                    if (setting.isProgressEnabled()) {
                        response.emitStarted();
                    }
                    
                    Map<String, Object> jsonMap = OpflowUtil.jsonStringToMap(msg);
                    
                    int number = ((Double) jsonMap.get("number")).intValue();
                    if (number < 0) throw new OpflowOperationException("number should be positive");
                    if (number > setting.getNumberMax()) {
                        throw new OpflowOperationException("number exceeding limit (" + setting.getNumberMax() + ")");
                    }
                    
                    FibonacciGenerator fibonacci = new FibonacciGenerator(number, 1, 9);
                    
                    // OPTIONAL
                    if (setting.isProgressEnabled()) {
                        while(fibonacci.next()) {
                            FibonacciGenerator.Result r = fibonacci.result();
                            response.emitProgress(r.getStep(), r.getNumber());
                        }
                    } else {
                        fibonacci.finish();
                    }

                    String result = OpflowUtil.jsonObjectToString(fibonacci.result());
                    LOG.debug("[-] Fibonacci finished with: '" + result + "'");

                    // MANDATORY
                    response.emitCompleted(result);
                } catch (final Exception ex) {
                    String errmsg = OpflowUtil.buildMap()
                            .put("exceptionClass", ex.getClass().getName())
                            .put("exceptionMessage", ex.getMessage())
                            .toString();
                    LOG.error("[-] Error message: " + errmsg);
                    
                    // MANDATORY
                    response.emitFailed(errmsg);
                }
                
                return null;
            }
        });
        
        return info;
    }
    
    public String checkState() {
        return worker.check().getConnectionState() == 1 ? "opened" : "closed";
    }
    
    public FibonacciSetting getSetting() {
        return setting;
    }
    
    public void close() {
        worker.close();
    }
    
    public static void main(String[] argv) throws Exception {
        final FibonacciRpcWorker rpc = new FibonacciRpcWorker();
        System.out.println("Fibonacci RPC Worker start: ");
        rpc.process();
        System.out.println("[*] Waiting for message. To exit press CTRL+C");
    }
}
