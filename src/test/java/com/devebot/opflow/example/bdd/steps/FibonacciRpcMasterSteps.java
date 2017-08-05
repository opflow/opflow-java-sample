package com.devebot.opflow.example.bdd.steps;

import com.devebot.opflow.OpflowRpcRequest;
import com.devebot.opflow.OpflowRpcResult;
import com.devebot.opflow.OpflowUtil;
import com.devebot.opflow.example.FibonacciGenerator;
import com.devebot.opflow.example.FibonacciRpcMaster;
import com.devebot.opflow.exception.OpflowConstructorException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
public class FibonacciRpcMasterSteps {
    
    private final JsonParser jsonParser = new JsonParser();
    private FibonacciRpcMaster master;
    private final Map<String, OpflowRpcRequest> requests = new HashMap<String, OpflowRpcRequest>();
    private final Map<String, Integer> inputs = new HashMap<String, Integer>();
    
    @Given("a Fibonacci master")
    public void givenAFibonacciMaster() throws OpflowConstructorException {
        master = new FibonacciRpcMaster();
    }
    
    @When("I make a request named $requestName with number: $number")
    public void makeRequest(@Named("requestName") final String requestName, @Named("number") final int number) {
        inputs.put(requestName, number);
        requests.put(requestName, master.request(number));
    }

    @Then("the request $requestName should finished successfully")
    public void checkRequestOutput(@Named("requestName") final String requestName) {
        OpflowRpcResult output = OpflowUtil.exhaustRequest(requests.get(requestName));
        JsonObject jsonObject = (JsonObject)jsonParser.parse(output.getValueAsString());
        
        int number = Integer.parseInt(jsonObject.get("number").toString());
        assertThat(number, equalTo(inputs.get(requestName)));
        
        FibonacciGenerator fibGen = new FibonacciGenerator(number);
        FibonacciGenerator.Result fibResult = fibGen.finish();
        
        int step = Integer.parseInt(jsonObject.get("step").toString());
        assertThat(step, equalTo(fibResult.getStep()));
        assertThat(step, equalTo(output.getProgress().length));
        
        long value = Long.parseLong(jsonObject.get("value").toString());
        assertThat(value, equalTo(fibResult.getValue()));
    }
}
