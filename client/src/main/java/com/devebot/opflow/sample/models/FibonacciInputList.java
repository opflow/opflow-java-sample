package com.devebot.opflow.sample.models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author drupalex
 */
public class FibonacciInputList {
    private final List<FibonacciInput> list = new ArrayList<FibonacciInput>();

    public FibonacciInputList() {
    }

    public FibonacciInputList(List<FibonacciInput> init) {
        list.addAll(init);
    }

    public List<FibonacciInput> getList() {
        List<FibonacciInput> copied = new ArrayList<FibonacciInput>();
        copied.addAll(list);
        return copied;
    }
    
    public void add(FibonacciInput packet) {
        list.add(packet);
    }
}
