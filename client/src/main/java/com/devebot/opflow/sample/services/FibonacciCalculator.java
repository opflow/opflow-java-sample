package com.devebot.opflow.sample.services;

import com.devebot.opflow.sample.models.FibonacciInput;
import com.devebot.opflow.sample.models.FibonacciInputList;
import com.devebot.opflow.sample.models.FibonacciOutput;
import com.devebot.opflow.sample.models.FibonacciOutputList;

/**
 *
 * @author drupalex
 */
public interface FibonacciCalculator {
    FibonacciOutput calc(int number);
    FibonacciOutput calc(FibonacciInput data);
    FibonacciOutputList calc(FibonacciInputList list);
}
