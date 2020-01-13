package com.devebot.opflow.sample.models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author drupalex
 */
public class FibonacciInputList implements FibonacciIdentifiable {

    private final String requestId;
    private final List<FibonacciInputItem> list = new ArrayList<>();

    public FibonacciInputList() {
        this(null, null);
    }

    public FibonacciInputList(List<FibonacciInputItem> init) {
        this(init, null);
    }
    
    public FibonacciInputList(List<FibonacciInputItem> init, String requestId) {
        this.requestId = requestId;
        if (init != null) {
            list.addAll(init);
        }
    }

    public List<FibonacciInputItem> getList() {
        List<FibonacciInputItem> copied = new ArrayList<>();
        copied.addAll(list);
        return copied;
    }
    
    public void add(FibonacciInputItem packet) {
        list.add(packet);
    }

    @Override
    public String getRequestId() {
        return this.requestId;
    }
}
