package com.eternal130.tfcaf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Util {

    public static final int[] operations = { -15, -9, -6, -3, 2, 7, 13, 16 };
    public static final Map<Integer, int[]> steps = new HashMap<>();
    public static final Map<Integer, Integer> operationsTfc = new HashMap<>();

    public static int nextOperationOffset(int Difference, int[] lastRules, int[] itemRules) {
        // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":差值减偏移值{}", Difference);
        if (Difference + operations[lastRules[2]] + operations[lastRules[1]] == 0
            && operationsTfc.get(itemRules[0]) == lastRules[1]
            && operationsTfc.get(itemRules[1]) == lastRules[2]) {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":完成前两步");
            return lastRules[0];
        } else if (Difference + operations[lastRules[2]] == 0 && operationsTfc.get(itemRules[0]) == lastRules[2]) {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":完成一步");
            return lastRules[1];
        } else if (Difference == 0) {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":完成对齐");
            return lastRules[2];
        }
//        TFCAutoForging.LOG.info("未对齐");
//        TFCAutoForging.LOG.info("差值:{}", Difference + operations[lastRules[2]] + operations[lastRules[1]]);
//        TFCAutoForging.LOG.info("倒数第一步{}与要求倒二{}", operationsTfc.get(itemRules[0]), lastRules[1]);
//        TFCAutoForging.LOG.info("倒数第二步{}与要求倒三{}", operationsTfc.get(itemRules[1]), lastRules[2]);
        int[] step = steps.get(Difference);
        for (int i = 0; i < 7; i++) {
            if (step[7 - i] > 0) {
                return 7 - i;
            }
            if (step[i] > 0) {
                return i;
            }
        }
        return -1;
    }

    public static void preCalculator() {
        for (int i = -100; i <= 140; i++) {
            steps.put(i, new int[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        }
        int[] dp = new int[241];
        for (int i = 0; i < 241; i++) {
            dp[i] = 100;
        }
        dp[100] = 0;
        for (int i = 140; i >= -100; i--) {
            foreachoperation(dp, i);
        }
        for (int i = -100; i <= 140; i++) {
            foreachoperation(dp, i);
        }
        operationsTfc.put(0, 3);
        operationsTfc.put(1, 0);
        operationsTfc.put(3, 4);
        operationsTfc.put(4, 5);
        operationsTfc.put(5, 6);
        operationsTfc.put(6, 7);
    }

    private static void foreachoperation(int[] dp, int i) {
        for (int op : operations) {
            if (i - op >= -100 && i - op <= 140) {
                if (dp[100 + i] > dp[100 + i - op] + 1) {
                    dp[100 + i] = dp[100 + i - op] + 1;
                    steps.put(
                        i,
                        steps.get(i - op)
                            .clone());
                    // System.out.println(Arrays.binarySearch(operations,op)+" "+op);
                    steps.get(i)[Arrays.binarySearch(operations, op)]++;
                }
            }
        }
    }
}
