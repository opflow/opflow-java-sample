package com.devebot.opflow.example.steps;

import com.devebot.opflow.OpflowBroker.ConsumerInfo;
import com.devebot.opflow.example.FibonacciRpcImpl;
import com.devebot.opflow.exception.OpflowConstructorException;
import java.util.HashMap;
import java.util.Map;
import org.jbehave.core.annotations.Given;

/**
 *
 * @author drupalex
 */
public class FibonacciRpcWorkerSteps {
    
    private final Map<String, FibonacciRpcImpl> workers = new HashMap<String, FibonacciRpcImpl>();
    private final Map<String, String> consumerTags = new HashMap<String, String>();
    
    @Given("a Fibonacci worker: $workerName")
    public void givenFibonacciWorkers(final String workerName) throws OpflowConstructorException {
        FibonacciRpcImpl worker = new FibonacciRpcImpl();
        workers.put(workerName, worker);
        ConsumerInfo info = worker.process();
        consumerTags.put(workerName, info.getConsumerTag());
    }
}
