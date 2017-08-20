package com.devebot.opflow.example.bdd.steps;

import com.devebot.opflow.OpflowEngine.ConsumerInfo;
import com.devebot.opflow.example.FibonacciRpcWorker;
import com.devebot.opflow.exception.OpflowConstructorException;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 *
 * @author drupalex
 */
public class FibonacciRpcWorkerSteps {
    
    private final Map<String, FibonacciRpcWorker> workers = new HashMap<String, FibonacciRpcWorker>();
    private final Map<String, String> consumerTags = new HashMap<String, String>();
    
    @Given("a Fibonacci RpcWorker($workerName)")
    public void givenFibonacciWorkers(final String workerName) throws OpflowConstructorException {
        FibonacciRpcWorker worker = new FibonacciRpcWorker();
        workers.put(workerName, worker);
        ConsumerInfo info = worker.process();
        consumerTags.put(workerName, info.getConsumerTag());
    }
    
    @When("I close RpcWorker($workerName)")
    public void closeRpcWorker(@Named("workerName") String workerName) {
        workers.get(workerName).close();
    }
    
    @Then("the RpcWorker($workerName)'s connection status is '$status'")
    public void checkRpcWorkerState(@Named("workerName") String workerName, @Named("status") String status) {
        assertThat(workers.get(workerName).checkState(), equalTo(status));
    }
}
