package com.devebot.opflow.sample.services;

import com.devebot.opflow.sample.business.FibonacciGenerator;
import com.devebot.opflow.sample.models.FibonacciInput;
import com.devebot.opflow.sample.models.FibonacciInputList;
import com.devebot.opflow.sample.models.FibonacciOutput;
import com.devebot.opflow.sample.models.FibonacciOutputList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author drupalex
 */
public class FibonacciCalculatorImpl implements FibonacciCalculator {

    @Override
    public FibonacciOutput calc(int number) {
        return new FibonacciGenerator(number).finish();
    }

    @Override
    public FibonacciOutput calc(FibonacciInput data) {
        return this.calc(data.getNumber());
    }

    @Override
    public FibonacciOutputList calc(FibonacciInputList list) {
        ArrayList<FibonacciOutput> results = new ArrayList<FibonacciOutput>();
        List<FibonacciInput> inputs = list.getList();
        for(FibonacciInput input: inputs) {
            results.add(this.calc(input.getNumber()));
        }
        return new FibonacciOutputList(results);
    }
}
