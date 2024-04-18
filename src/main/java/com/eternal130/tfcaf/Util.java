package com.eternal130.tfcaf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Util {

    // 下面这个变量用于计算,以及所有和锻造相关的功能都会用到
    public static final int[] operations = { -15, -9, -6, -3, 2, 7, 13, 16 };
    // 下面这个map保存计算好的锻造步骤,k为目标锻造值-当前锻造值-锻造偏移值,v为不同锻造步骤的数量,如{1,0,0,0,0,0,0,2}表示-15的步骤一次,16的步骤两次
    public static final Map<Integer, int[]> steps = new HashMap<>();
    // 下面两个map用于将锻造按钮的k和锻造步骤的k映射到operations上
    public static final Map<Integer, Integer> operationsTfc = new HashMap<>();
    public static final Map<Integer, Integer> buttonMapping = new HashMap<>();

    public static int nextOperationOffset(int Difference, int[] lastRules, int[] itemRules) {
        /**
         * 计算下一步步骤.
         * 
         * @param Difference 目标锻造值-当前锻造值-锻造偏移值
         * @param lastRules  锻造步骤要求
         * @param itemRules  最后三步步骤
         * @return 下一步锻造步骤在operations中的键值
         */
        // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":差值减偏移值{}", Difference);
        // 如果偏移差值+要求倒三步+要求倒二步==0,并且当前倒一步是要求倒二步,当前倒二步是要求倒三步
        // 这意味着在满足减去偏移值的基础上,又完成了要求的倒二和倒三,因此下一步就是倒一
        if (Difference + operations[lastRules[2]] + operations[lastRules[1]] == 0
            && operationsTfc.get(itemRules[0]) == lastRules[1]
            && operationsTfc.get(itemRules[1]) == lastRules[2]) {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":完成前两步");
            return lastRules[0];
            // 如果偏移差值+要求倒三步==0,并且当前倒一是要求倒三步
            // 这意味着满足减去偏移值的基础上,又完成了要求的倒数第三步,因此下一步是倒二
        } else if (Difference + operations[lastRules[2]] == 0 && operationsTfc.get(itemRules[0]) == lastRules[2]) {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":完成一步");
            return lastRules[1];
            // 如果偏移差值为0,意味着已经完成锻造要求外的其他步骤,因此下一步是锻造要求的倒数第三步
        } else if (Difference == 0) {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":完成对齐");
            return lastRules[2];
        }
        // TFCAutoForging.LOG.info("未对齐");
        // TFCAutoForging.LOG.info("差值:{}", Difference + operations[lastRules[2]] + operations[lastRules[1]]);
        // TFCAutoForging.LOG.info("倒数第一步{}与要求倒二{}", operationsTfc.get(itemRules[0]), lastRules[1]);
        // TFCAutoForging.LOG.info("倒数第二步{}与要求倒三{}", operationsTfc.get(itemRules[1]), lastRules[2]);
        // 上面判定没有通过,意味着还没有进入收尾步骤,下一步从查表获取的步骤列表中获取步骤,
        // 因为列表是有序的,因此从两侧查找,能够保证尽快靠近锻造要求数值
        int[] step = steps.get(Difference);
        for (int i = 0; i < 7; i++) {
            if (step[7 - i] > 0) {
                return 7 - i;
            }
            if (step[i] > 0) {
                return i;
            }
        }
        // 走到这里,说明没有下一步,返回-1,事件退出
        return -1;
    }

    public static void preCalculator() {
        // 使用动态规划计算最小步骤
        // tfc+锻造槽150,tfc应该也是150
        // tfc+中锻造初值在70到120之间
        // 锻造指针在0到150之间
        // target-current应该在-80到120之间,但因为查表是使用的值减去了锻造偏移,因此扩大了范围
        // -100<=target-current<=140
        // 0<=target-current+100<=240
        // dp[100+i]表示锻造差值为i时的最少锻造步骤数
        // dp[100]=0
        // dp[100+i]=Math.min(dp[80+i-operations[j]]) +1
        for (int i = -100; i <= 140; i++) {
            steps.put(i, new int[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        }
        int[] dp = new int[241];
        for (int i = 0; i < 241; i++) {
            dp[i] = 100;
        }
        dp[100] = 0;
        // 这里正反两次遍历dp数组,因为单次遍历有某些值无法到达
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
        buttonMapping.put(0, 3);
        buttonMapping.put(1, 2);
        buttonMapping.put(2, 1);
        buttonMapping.put(3, 0);
        buttonMapping.put(4, 4);
        buttonMapping.put(5, 5);
        buttonMapping.put(6, 6);
        buttonMapping.put(7, 7);
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
