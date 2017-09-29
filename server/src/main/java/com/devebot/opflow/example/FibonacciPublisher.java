package com.devebot.opflow.example;

import com.devebot.opflow.OpflowBuilder;
import com.devebot.opflow.OpflowPubsubHandler;
import com.devebot.opflow.exception.OpflowBootstrapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author drupalex
 */
public class FibonacciPublisher {
    private final static Logger LOG = LoggerFactory.getLogger(FibonacciPublisher.class);
    private final OpflowPubsubHandler handler;
    
    public FibonacciPublisher() throws OpflowBootstrapException {
        this(null);
    }
    
    public FibonacciPublisher(String propFile) throws OpflowBootstrapException {
        this.handler = OpflowBuilder.createPubsubHandler(propFile);
    }
    
    public void publish(int numberMax) {
        handler.publish("{ \"numberMax\": " + numberMax + " }");
    }
    
    public String checkState() {
        return handler.check().getConnectionState() == 1 ? "opened" : "closed";
    }
    
    public void close() {
        handler.close();
    }
    
    public static void main(String[] argv) throws Exception {
        final FibonacciPublisher pubsub = new FibonacciPublisher();
        
        System.out.println("[-] publish 20 messages");
        for(int i=20; i<40; i++) {
            pubsub.publish(i);
        }
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {}
        
        pubsub.close();
        System.out.println("[-] Pub/sub has been done!");
    }
}
