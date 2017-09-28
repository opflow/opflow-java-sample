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
}
