package com.devebot.opflow.sample.utils;

import java.util.Random;

/**
 *
 * @author acegik
 */
public class Randomizer {
    private static final Random RANDOM = new Random();
    
    public static int random(int min, int max) {
        return RANDOM.nextInt(max + 1 - min) + min;
    }
}
