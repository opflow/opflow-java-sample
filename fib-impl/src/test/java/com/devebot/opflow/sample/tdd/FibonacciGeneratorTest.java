package com.devebot.opflow.sample.tdd;

import com.devebot.opflow.sample.business.FibonacciGenerator;
import com.devebot.opflow.sample.models.FibonacciOutputItem;
import java.util.Random;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author drupalex
 */
public class FibonacciGeneratorTest {
    
    private FibonacciGenerator[] fibgen;
    
    @Before
    public void initGeneratorObjects() {
        // do something
    }
    
    @After
    public void destroyGeneratorObjects() {
        // do something
    }
    
    @Test
    public void testAListOfGeneratorObjects() {
        fibgen = new FibonacciGenerator[10];
        
        for(int i = 0; i<fibgen.length; i++) {
            fibgen[i] = new FibonacciGenerator(i);
        }
        
        long[] fibseq = getFibonacciSequence(fibgen.length);
        FibonacciOutputItem[] testresult = new FibonacciOutputItem[fibgen.length];
        for(int i = 0; i<fibgen.length; i++) {
            testresult[i] = fibgen[i].finish();
            Assert.assertEquals(testresult[i].getNumber(), i);
            Assert.assertEquals(testresult[i].getValue(), fibseq[i]);
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testForNegativeNumber() {
        FibonacciGenerator fg = new FibonacciGenerator(-1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testForExceedingLimit() {
        FibonacciGenerator fg = new FibonacciGenerator(51);
    }
    
    private static final Random RANDOM = new Random();
    
    private static int random(int min, int max) {
        return RANDOM.nextInt(max + 1 - min) + min;
    }
    
    private static long[] getFibonacciSequence(int length) {
        long result[] = new long[length];
        result[0] = 0;
        result[1] = 1;
        for(int i=2; i<length; i++) {
            result[i] = result[i-1] + result[i-2];
        }
        return result;
    }
}
