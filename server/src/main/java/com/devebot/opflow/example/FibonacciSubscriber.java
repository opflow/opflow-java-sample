package com.devebot.opflow.example;

import com.devebot.opflow.OpflowEngine;
import com.devebot.opflow.OpflowBuilder;
import com.devebot.opflow.OpflowConfig;
import com.devebot.opflow.OpflowMessage;
import com.devebot.opflow.OpflowPubsubHandler;
import com.devebot.opflow.OpflowPubsubListener;
import com.devebot.opflow.OpflowTask;
import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.exception.OpflowOperationException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author drupalex
 */
public class FibonacciSubscriber {

    private final static Logger LOG = LoggerFactory.getLogger(FibonacciSubscriber.class);
    private final JsonParser jsonParser = new JsonParser();
    private final OpflowPubsubHandler handler;
    private final FibonacciSetting setting;
    private final OpflowPubsubListener listener = new OpflowPubsubListener() {
        @Override
        public void processMessage(OpflowMessage message) throws IOException {
            String content = new String(message.getBody(), "UTF-8");
            System.out.println(" [-] Received setting: '" + content + "'");
            JsonObject jsonObject = (JsonObject)jsonParser.parse(content);
            int numberMax = Integer.parseInt(jsonObject.get("numberMax").toString());

            if (countdown != null) countdown.check();

            if (numberMax <= 0) throw new OpflowOperationException("numberMax should be positive");
            if (numberMax > 50) throw new OpflowOperationException("numberMax exceeding limit (50)");
            
            if (0< numberMax && numberMax <= 50 && setting != null) {
                setting.setNumberMax(numberMax);
                System.out.println(" [-] numberMax: '" + numberMax + "' has been set to setting object");
            }
        }
    };
    private OpflowTask.Countdown countdown;
    
    public FibonacciSubscriber() throws OpflowBootstrapException {
        this(null, null);
    }
    
    public FibonacciSubscriber(FibonacciSetting setting) throws OpflowBootstrapException {
        this(setting, null);
    }
    
    public FibonacciSubscriber(String propFile) throws OpflowBootstrapException {
        this(null, propFile);
    }
    
    public FibonacciSubscriber(FibonacciSetting setting, String propFile) throws OpflowBootstrapException {
        Properties props = new Properties();
        props.setProperty("opflow.pubsub.uri", "");
        props.setProperty("opflow.pubsub.host", "rabbit-node3");
        props.setProperty("opflow.pubsub.port", "5673");
        props.setProperty("opflow.pubsub.username", "master");
        props.setProperty("opflow.pubsub.password", "zaq123edcx");
        props.setProperty("opflow.pubsub.pkcs12File", "/home/drupalex/projects/opflow/opflow-weaver/sslstore/client/opflow-test-java.keycert.p12");
        props.setProperty("opflow.pubsub.pkcs12Passphrase", "zaq123edcx");
//        props.setProperty("opflow.pubsub.caCertFile", "/home/drupalex/projects/opflow/opflow-weaver/sslstore/ca/cacert.pem");
        props.setProperty("opflow.pubsub.serverCertFile", "/home/drupalex/projects/opflow/opflow-weaver/sslstore/server/rabbit-node3.cert.pem");
        // props.setProperty("opflow.pubsub.trustStoreFile", "/home/drupalex/projects/opflow/opflow-weaver/sslstore/server/rabbit-node3.tks");
        // props.setProperty("opflow.pubsub.trustPassphrase", "rabbitstore");
        this.handler = OpflowBuilder.createPubsubHandler(OpflowConfig.mergeConfiguration(null, props), propFile, true);
        this.setting = (setting != null) ? setting : new FibonacciSetting();
    }
    
    public void publish(int numberMax) {
        handler.publish("{ \"numberMax\": " + numberMax + " }");
    }
    
    public OpflowEngine.ConsumerInfo subscribe() {
        return this.subscribe(1)[0];
    }
    
    public OpflowEngine.ConsumerInfo[] subscribe(int consumerNumber) {
        if (consumerNumber > 0) {
            OpflowEngine.ConsumerInfo[] consumerInfos = new OpflowEngine.ConsumerInfo[consumerNumber];
            for(int i=0; i<consumerNumber; i++) {
                consumerInfos[i] = handler.subscribe(listener);
            }
            return consumerInfos;
        }
        return null;
    }
    
    public int getRedeliveredLimit() {
        return handler.getRedeliveredLimit();
    }
    
    public int countSubscriber() {
        return handler.getExecutor().countQueue(handler.getSubscriberName());
    }
    
    public int countRecyclebin() {
        return handler.getExecutor().countQueue(handler.getRecyclebinName());
    }
    
    public int getNumberOfConsumers() {
        return handler.getExecutor().defineQueue(handler.getSubscriberName()).getConsumerCount();
    }
    
    public String checkState() {
        return handler.check().getConnectionState() == 1 ? "opened" : "closed";
    }
    
    public void close() {
        handler.close();
    }
    
    public OpflowTask.Countdown getCountdown() {
        return countdown;
    }
    
    public void setCountdown(OpflowTask.Countdown countdown) {
        this.countdown = countdown;
    }

    public static void main(String[] argv) throws Exception {
        final FibonacciSubscriber pubsub = new FibonacciSubscriber();
        
        System.out.println("[+] Start a Subscriber");
        pubsub.subscribe();
        System.out.println("[*] Waiting for message. To exit press CTRL+C");
    }
}
