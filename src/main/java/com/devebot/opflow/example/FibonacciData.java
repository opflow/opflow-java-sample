package com.devebot.opflow.example;

import com.devebot.opflow.OpflowRpcRequest;

/**
 *
 * @author drupalex
 */
public class FibonacciData {
    
    public static class Pair {
        private final int index;
        private final int number;
        private final OpflowRpcRequest session;
        
        public Pair(int index, int number, OpflowRpcRequest session) {
            this.index = index;
            this.number = number;
            this.session = session;
        }
        
        public int getIndex() {
            return index;
        }
        
        public int getNumber() {
            return number;
        }
        
        public OpflowRpcRequest getSession() {
            return session;
        }
    }
    
    public static class Setting {
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
}
