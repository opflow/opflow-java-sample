package com.devebot.opflow.sample;

import com.devebot.opflow.sample.models.FibonacciInputItem;
import com.devebot.opflow.sample.models.FibonacciInputList;
import com.devebot.opflow.sample.models.FibonacciOutputItem;
import com.devebot.opflow.sample.models.FibonacciOutputList;
import com.devebot.opflow.sample.services.FibonacciCalculator;
import com.devebot.opflow.sample.utils.CommonUtil;
import com.devebot.opflow.sample.utils.Randomizer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author drupalex
 */
public class FibonacciExample {
    public static void main(String[] argv) throws Exception {
        final FibonacciCalculator fib = new FibonacciCalculatorSimple();
        System.out.println("[+] Calc a fibonacci:");
        FibonacciOutputItem output = fib.calc(45);
        System.out.println("[-] output: " + output.getValue());
    }
    
    private static class FibonacciCalculatorSimple implements FibonacciCalculator {

        @Override
        public FibonacciOutputItem calc(int number) {
            return new FibonacciGenerator(number).finish();
        }

        @Override
        public FibonacciOutputItem calc(FibonacciInputItem data) {
            return this.calc(data.getNumber());
        }

        @Override
        public FibonacciOutputList calc(FibonacciInputList list) {
            ArrayList<FibonacciOutputItem> results = new ArrayList<>();
            List<FibonacciInputItem> inputs = list.getList();
            for(FibonacciInputItem input: inputs) {
                results.add(this.calc(input.getNumber()));
            }
            return new FibonacciOutputList(results);
        }
        
        @Override
        public FibonacciOutputList calc(List<FibonacciInputItem> list) {
            return this.calc(new FibonacciInputList(list));
        }
    }
    
    private static class FibonacciGenerator {
        private int n;
        private int c = 0;
        private long f = 0, f_1 = 0, f_2 = 0;
        private int m;
        private int M;

        public FibonacciGenerator(int number) {
            this(number, 0);
        }

        public FibonacciGenerator(int number, int max) {
            this(number, 0, max);
        }

        public FibonacciGenerator(int number, int min, int max) {
            this.n = number;
            this.m = min;
            this.M = max;
        }

        public boolean next() {
            if (0 <= this.m && this.m < this.M) {
                CommonUtil.sleep(Randomizer.random(this.m, this.M));
            }
            if (c >= n) return false;
            if (++c < 2) {
                f = c;
            } else {
                f_2 = f_1; f_1 = f; f = f_1 + f_2;
            }
            return true;
        }

        public FibonacciOutputItem result() {
            return new FibonacciOutputItem(f, c, n);
        }

        public FibonacciOutputItem finish() {
            while(next()) {}
            return result();
        }
    }
}
