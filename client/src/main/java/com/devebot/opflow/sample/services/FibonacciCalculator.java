package com.devebot.opflow.sample.services;

import com.devebot.opflow.sample.models.FibonacciData;
import com.devebot.opflow.sample.models.FibonacciResult;

/**
 *
 * @author drupalex
 */
public interface FibonacciCalculator {
    FibonacciResult calc(int number);
    FibonacciResult calc(FibonacciData data);
}
