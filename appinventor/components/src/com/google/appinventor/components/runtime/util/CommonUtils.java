package com.google.appinventor.components.runtime.util;

import java.util.Random;

public class CommonUtils {
    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            return 0;
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
