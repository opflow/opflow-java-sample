package com.devebot.opflow.sample.models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author drupalex
 */
public class FibonacciOutputList {
    private final List<FibonacciOutput> list = new ArrayList<FibonacciOutput>();

    public FibonacciOutputList() {
    }

    public FibonacciOutputList(List<FibonacciOutput> init) {
        list.addAll(init);
    }

    public List<FibonacciOutput> getList() {
        List<FibonacciOutput> copied = new ArrayList<FibonacciOutput>();
        copied.addAll(list);
        return copied;
    }
    
    public void add(FibonacciOutput item) {
        list.add(item);
    }
}
