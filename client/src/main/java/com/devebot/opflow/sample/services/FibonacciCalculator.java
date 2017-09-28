package com.devebot.opflow.sample.services;

import com.devebot.opflow.sample.models.FibonacciResult;
import com.devebot.opflow.sample.models.FibonacciPacket;

/**
 *
 * @author drupalex
 */
public interface FibonacciCalculator {
    FibonacciResult calc(int number);
    FibonacciResult calc(FibonacciPacket data);
}
