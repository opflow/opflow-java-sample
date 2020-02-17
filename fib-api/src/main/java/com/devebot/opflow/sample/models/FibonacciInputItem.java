package com.devebot.opflow.sample.models;

import com.devebot.opflow.OpflowUUID;

/**
 *
 * @author drupalex
 */
public class FibonacciInputItem implements FibonacciIdentifiable {

    private final String requestId;
    private final int number;

    public FibonacciInputItem(int number) {
        this(number, null);
    }

    public FibonacciInputItem(int number, String requestId) {
        this.number = number;
        if (requestId == null) {
            this.requestId = OpflowUUID.getBase64ID();
        } else {
            this.requestId = requestId;
        }
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String getRequestId() {
        return requestId;
    }
}
