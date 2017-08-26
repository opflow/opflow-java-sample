package com.devebot.opflow.example;

/**
 *
 * @author drupalex
 */
public class FibonacciSetting {
    
    private int numberMax = 40;

    public int getNumberMax() {
        return numberMax;
    }

    public synchronized void setNumberMax(int numberMax) {
        this.numberMax = numberMax;
    }
}
