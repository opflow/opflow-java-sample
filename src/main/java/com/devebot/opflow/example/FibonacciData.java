package com.devebot.opflow.example;

import com.devebot.opflow.OpflowRpcRequest;

/**
 *
 * @author drupalex
 */
public class FibonacciData {
    
    public static class Pair {
        private final int number;
        private final OpflowRpcRequest session;
        
        public Pair(int number, OpflowRpcRequest session) {
            this.number = number;
            this.session = session;
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

        public int getNumberMax() {
            return numberMax;
        }

        public synchronized void setNumberMax(int numberMax) {
            this.numberMax = numberMax;
        }
    }
}
