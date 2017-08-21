package com.devebot.opflow.example.bdd.steps;

import com.devebot.opflow.OpflowEngine.ConsumerInfo;
import com.devebot.opflow.OpflowTask;
import com.devebot.opflow.OpflowUtil;
import com.devebot.opflow.example.FibonacciPubsubHandler;
import com.devebot.opflow.exception.OpflowConstructorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author drupalex
 */
public class FibonacciPubsubSteps {
    
    private final static Logger LOG = LoggerFactory.getLogger(FibonacciPubsubSteps.class);
    private final Map<String, FibonacciPubsubHandler> pubsubs = new HashMap<String, FibonacciPubsubHandler>();
    private final Map<String, ConsumerInfo> consumerTags = new HashMap<String, ConsumerInfo>();
    private final List<Integer> failedNumbers = new ArrayList<Integer>();
    
    @Given("a Fibonacci PubsubHandler($pubsubName)")
    public void givenDefaultFibonacciPubsubHandler(@Named("pubsubName") final String pubsubName) 
            throws OpflowConstructorException {
        givenFibonacciPubsubHandler(pubsubName, null);
    }
    
    @Given("a Fibonacci PubsubHandler($pubsubName) with properties file: '$propFile'")
    public void givenFibonacciPubsubHandler(@Named("pubsubName") final String pubsubName, 
            @Named("propFile") final String propFile) throws OpflowConstructorException {
        FibonacciPubsubHandler pubsub = (propFile == null) ? 
                new FibonacciPubsubHandler() : new FibonacciPubsubHandler(propFile);
        pubsub.setCountdown(new OpflowTask.Countdown());
        pubsubs.put(pubsubName, pubsub);
        failedNumbers.clear();
    }
    
    @Given("'$number' subscribers in PubsubHandler($pubsubName)")
    public void givenSubscribers(@Named("pubsubName") final String pubsubName, 
            @Named("number") final int number) throws OpflowConstructorException {
        pubsubs.get(pubsubName).subscribe(number);
    }
    
    @Then("PubsubHandler($pubsubName) has exactly '$number' consumers")
    public void countSubscribers(@Named("pubsubName") final String pubsubName, 
            @Named("number") final int number) throws OpflowConstructorException {
        assertThat(pubsubs.get(pubsubName).getNumberOfConsumers(), equalTo(number));
    }
    
    @Given("a failed number arrays '$failedNumbers'")
    public void givenAFailedNumberArrays(@Named("pubsubName") final String pubsubName, 
            @Named("failedNumbers") final String failedNumbersStr) throws OpflowConstructorException {
        String[] failedNumbersArr = OpflowUtil.splitByComma(failedNumbersStr);
        Integer[] failedNumbersInt = new Integer[failedNumbersArr.length];
        for(int i=0; i<failedNumbersArr.length; i++) {
            failedNumbersInt[i] = Integer.parseInt(failedNumbersArr[i]);
        }
        failedNumbers.addAll(Arrays.asList(failedNumbersInt));
    }
    
    @When("I publish '$total' random messages to PubsubHandler($pubsubName)")
    public void publishNumber(@Named("total") final int total, 
            @Named("pubsubName") final String pubsubName) {
        if (total < failedNumbers.size()) throw new IllegalArgumentException();
        FibonacciPubsubHandler pubsub = pubsubs.get(pubsubName);
        pubsub.getCountdown().reset(total + pubsub.getRedeliveredLimit() * failedNumbers.size());
        int count = 0;
        while(0 < failedNumbers.size() && (count + failedNumbers.size()) < total) {
            if (RANDOM.nextBoolean()) {
                pubsub.publish(random(20, 40));
            } else {
                pubsub.publish(failedNumbers.remove(0));
            }
            count++;
        }
        while(!failedNumbers.isEmpty()) {
            pubsub.publish(failedNumbers.remove(0));
        }
        for(int i=count; i<total; i++) {
            pubsub.publish(random(20, 40));
        }
    }
    
    @When("waiting for subscriber of PubsubHandler($pubsubName) finish")
    public void waitSubscriberFinish(@Named("pubsubName") final String pubsubName) {
        pubsubs.get(pubsubName).getCountdown().bingo();
    }
    
    @Then("PubsubHandler($pubsubName) receives '$total' messages")
    public void totalReceivedMessages(@Named("pubsubName") final String pubsubName, 
            @Named("total") final int total) {
        System.out.println("[*] Received total: " + pubsubs.get(pubsubName).getCountdown().getCount());
        assertThat(pubsubs.get(pubsubName).getCountdown().getCount(), equalTo(total));
    }
    
    @When("I close PubsubHandler($pubsubName)")
    public void closePubsubHandler(@Named("pubsubName") String pubsubName) {
        pubsubs.get(pubsubName).close();
    }
    
    @Then("the PubsubHandler($pubsubName)'s connection is '$status'")
    public void checkPubsubHandler(@Named("pubsubName") String pubsubName, @Named("status") String status) {
        assertThat(pubsubs.get(pubsubName).checkState(), equalTo(status));
    }
    
    private static final Random RANDOM = new Random();
    
    private static int random(int min, int max) {
        return RANDOM.nextInt(max + 1 - min) + min;
    }
}
