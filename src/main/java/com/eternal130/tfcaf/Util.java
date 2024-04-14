package com.eternal130.tfcaf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Util {

    public static final int[] operations = { -15, -9, -6, -3, 2, 7, 13, 16 };
    public static final Map<Integer, int[]> steps = new HashMap<>();

    public static void main(String[] args) {
        preCalculator();
        // System.out.println(steps);
    }

    public static int nextOperation() {
        return 0;
    }

    public static void preCalculator() {
        for (int i = -80; i <= 120; i++) {
            steps.put(i, new int[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        }
        int[] dp = new int[201];
        for (int i = 0; i < 201; i++) {
            dp[i] = 100;
        }
        dp[80] = 0;
        for (int i = 120; i >= -80; i--) {
            foreachoperation(dp, i);
        }
        for (int i = -80; i <= 120; i++) {
            foreachoperation(dp, i);
        }
    }

    private static void foreachoperation(int[] dp, int i) {
        for (int op : operations) {
            if (i - op >= -80 && i - op <= 120) {
                if (dp[80 + i] > dp[80 + i - op] + 1) {
                    dp[80 + i] = dp[80 + i - op] + 1;
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
