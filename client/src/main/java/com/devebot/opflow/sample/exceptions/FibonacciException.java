package com.devebot.opflow.sample.exceptions;

/**
 *
 * @author drupalex
 */
public class FibonacciException extends Exception {

    public FibonacciException() {
    }

    public FibonacciException(String message) {
        super(message);
    }

    public FibonacciException(String message, Throwable cause) {
        super(message, cause);
    }

    public FibonacciException(Throwable cause) {
        super(cause);
    }

    public FibonacciException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
