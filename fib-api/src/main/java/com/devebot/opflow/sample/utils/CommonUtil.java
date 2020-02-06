package com.devebot.opflow.sample.utils;

/**
 *
 * @author acegik
 */
public class CommonUtil {
    public static void sleep(long d) {
        if (d > 0) {
            try {
                Thread.sleep(d);
            } catch(InterruptedException ie) {}
        }
    }
    
    public static int countDigit(long number) {
        int count = 0;
        while (number > 0) {
            number = number / 10;
            count++;
        }
        return count;        
    }
}
