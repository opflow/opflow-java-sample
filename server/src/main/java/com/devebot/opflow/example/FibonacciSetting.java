package com.devebot.opflow.example;

/**
 *
 * @author drupalex
 */
public class FibonacciSetting {
    
    private int numberMax = 40;
    private boolean progressEnabled = true;

    public int getNumberMax() {
        return numberMax;
    }

    public synchronized void setNumberMax(int numberMax) {
        this.numberMax = numberMax;
    }

    public boolean isProgressEnabled() {
        return progressEnabled;
    }

    public void setProgressEnabled(boolean progressEnabled) {
        this.progressEnabled = progressEnabled;
    }
    
}
