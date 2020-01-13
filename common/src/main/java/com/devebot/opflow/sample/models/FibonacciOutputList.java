package com.devebot.opflow.sample.models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author drupalex
 */
public class FibonacciOutputList {
    private final List<FibonacciOutputItem> list = new ArrayList<>();

    public FibonacciOutputList() {
    }

    public FibonacciOutputList(List<FibonacciOutputItem> init) {
        list.addAll(init);
    }

    public List<FibonacciOutputItem> getList() {
        List<FibonacciOutputItem> copied = new ArrayList<>();
        copied.addAll(list);
        return copied;
    }
    
    public void add(FibonacciOutputItem item) {
        list.add(item);
    }
}
