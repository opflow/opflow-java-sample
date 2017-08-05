package com.devebot.opflow.example.bdd.steps;

import com.devebot.opflow.OpflowBroker.ConsumerInfo;
import com.devebot.opflow.example.FibonacciRpcWorker;
import com.devebot.opflow.exception.OpflowConstructorException;
import java.util.HashMap;
import java.util.Map;
import org.jbehave.core.annotations.Given;

/**
 *
 * @author drupalex
 */
public class FibonacciRpcWorkerSteps {
    
    private final Map<String, FibonacciRpcWorker> workers = new HashMap<String, FibonacciRpcWorker>();
    private final Map<String, String> consumerTags = new HashMap<String, String>();
    
    @Given("a Fibonacci worker: $workerName")
    public void givenFibonacciWorkers(final String workerName) throws OpflowConstructorException {
        FibonacciRpcWorker worker = new FibonacciRpcWorker();
        workers.put(workerName, worker);
        ConsumerInfo info = worker.process();
        consumerTags.put(workerName, info.getConsumerTag());
    }
}
