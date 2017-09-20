package com.devebot.opflow.example;

import com.devebot.opflow.OpflowLoader;
import com.devebot.opflow.OpflowMessage;
import com.devebot.opflow.OpflowPubsubListener;
import com.devebot.opflow.OpflowRpcListener;
import com.devebot.opflow.OpflowRpcResponse;
import com.devebot.opflow.OpflowServerlet;
import com.devebot.opflow.OpflowUtil;
import com.devebot.opflow.exception.OpflowOperationException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author drupalex
 */
public class FibonacciServer {
    private final static Logger LOG = LoggerFactory.getLogger(FibonacciServer.class);
    
    public static void main(String[] argv) throws Exception {
        final JsonParser jsonParser = new JsonParser();
        
        final FibonacciSetting setting = new FibonacciSetting();
        
        final OpflowServerlet server = 
        OpflowLoader.createServerlet(new OpflowServerlet.ListenerMap(new OpflowPubsubListener() {
            // Configuration pub/sub
            @Override
            public void processMessage(OpflowMessage message) throws IOException {
                String content = new String(message.getBody(), "UTF-8");
                System.out.println(" [-] Received setting: '" + content + "'");
                JsonObject jsonObject = (JsonObject)jsonParser.parse(content);
                int numberMax = Integer.parseInt(jsonObject.get("numberMax").toString());

                if (numberMax <= 0) throw new OpflowOperationException("numberMax should be positive");
                if (numberMax > 50) throw new OpflowOperationException("numberMax exceeding limit (50)");

                if (0< numberMax && numberMax <= 50 && setting != null) {
                    setting.setNumberMax(numberMax);
                    System.out.println(" [-] numberMax: '" + numberMax + "' has been set to setting object");
                }
            }
        }, new OpflowServerlet.RpcWorkerEntry[] {
            // Fibonacci function
            new OpflowServerlet.RpcWorkerEntry("fibonacci", new OpflowRpcListener() {
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
            })
        }), "server.properties");
        
        System.out.println("FibonacciServer start: ");
        server.start();
        System.out.println("[*] Waiting for message. To exit press CTRL+C");
    }
}
