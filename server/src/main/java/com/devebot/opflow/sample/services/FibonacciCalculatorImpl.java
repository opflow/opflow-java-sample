package com.devebot.opflow.sample.services;

import com.devebot.opflow.sample.business.FibonacciGenerator;
import com.devebot.opflow.sample.models.FibonacciResult;
import com.devebot.opflow.sample.models.FibonacciData;

/**
 *
 * @author drupalex
 */
public class FibonacciCalculatorImpl implements FibonacciCalculator {

    @Override
    public FibonacciResult calc(int number) {
        return new FibonacciGenerator(number).finish();
    }

    @Override
    public FibonacciResult calc(FibonacciData data) {
        return this.calc(data.getNumber());
    }
}
