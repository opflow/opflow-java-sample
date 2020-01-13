package com.devebot.opflow.sample.models;

/**
 *
 * @author drupalex
 */
public class FibonacciOutputItem {
    
    private final long value;
    private final int step;
    private final int number;

    public FibonacciOutputItem(long value, int step, int number) {
        this.value = value;
        this.step = step;
        this.number = number;
    }

    public long getValue() {
        return value;
    }

    public int getStep() {
        return step;
    }

    public int getNumber() {
        return number;
    }
}
