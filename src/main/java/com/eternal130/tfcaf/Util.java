package com.eternal130.tfcaf;

import net.dries007.tfc.common.capabilities.forge.ForgeSteps;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Util {

    // 下面这个变量用于计算,以及所有和锻造相关的功能都会用到
    public static final int[] operations = {-15, -9, -6, -3, 2, 7, 13, 16};
    // 下面这个map保存计算好的锻造步骤,k为目标锻造值-当前锻造值-锻造偏移值,v为不同锻造步骤的数量,如{1,0,0,0,0,0,0,2}表示-15的步骤一次,16的步骤两次
    public static final Map<Integer, int[]> steps = new HashMap<>();
    // 下面两个map用于将锻造按钮的k和锻造要求步骤的k映射到operations上
    public static final Map<Integer, Integer> operationsTfc = new HashMap<>();
    public static final Map<Integer, Integer> buttonMapping = new HashMap<>();
    // 下面这个map用于将最后三步的步骤重映射到operations上
    public static final Map<Integer, Integer> stepTFC = new HashMap<>();

    public static int nextOperationOffset(int Difference, int[] lastRules, ForgeSteps itemRules) {
        /**
         * 计算下一步步骤.
         *
         * @param Difference 目标锻造值-当前锻造值-锻造偏移值
         * @param lastRules  锻造步骤要求
         * @param itemRules  最后三步步骤
         * @return 下一步锻造步骤在operations中的键值
         */
//        TFCAutoForging.logger.info(TFCAutoForging.MODID + ":差值减偏移值{}", Difference);
//        TFCAutoForging.logger.info("要求三步:{} {} {}", lastRules[0],lastRules[1],lastRules[2]);
//        TFCAutoForging.logger.info("最后三步:{} {} {}", buttonMapping.get(itemRules.getStep(2).ordinal()),buttonMapping.get(itemRules.getStep(1).ordinal()),buttonMapping.get(itemRules.getStep(0).ordinal()));
        // 如果偏移差值+要求倒三步+要求倒二步==0,并且当前倒一步是要求倒二步,当前倒二步是要求倒三步
        // 这意味着在满足减去偏移值的基础上,又完成了要求的倒二和倒三,因此下一步就是倒一
        if (Difference + operations[lastRules[2]] + operations[lastRules[1]] == 0
                && buttonMapping.get(itemRules.last().ordinal()) == lastRules[1]
                && buttonMapping.get(itemRules.secondLast().ordinal()) == lastRules[2]) {
//             TFCAutoForging.logger.info(TFCAutoForging.MODID + ":完成前两步");
            return lastRules[0];
            // 如果偏移差值+要求倒三步==0,并且当前倒一是要求倒三步
            // 这意味着满足减去偏移值的基础上,又完成了要求的倒数第三步,因此下一步是倒二
        } else if (Difference + operations[lastRules[2]] == 0 && buttonMapping.get(itemRules.last().ordinal()) == lastRules[2]) {
//             TFCAutoForging.logger.info(TFCAutoForging.MODID + ":完成一步");
            return lastRules[1];
            // 如果偏移差值为0,意味着已经完成锻造要求外的其他步骤,因此下一步是锻造要求的倒数第三步
        } else if (Difference == 0) {
//             TFCAutoForging.logger.info(TFCAutoForging.MODID + ":完成对齐");
            return lastRules[2];
        }
//         TFCAutoForging.logger.info("未对齐");
//         TFCAutoForging.logger.info("差值:{}", Difference + operations[lastRules[2]] + operations[lastRules[1]]);
//         TFCAutoForging.logger.info("倒数第一步{}与要求倒二{}", operationsTfc.get(itemRules.getStep(2).ordinal()), lastRules[1]);
//         TFCAutoForging.logger.info("倒数第二步{}与要求倒三{}", operationsTfc.get(itemRules.getStep(1).ordinal()), lastRules[2]);
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
        // tfc+锻造槽150,tfc应该也是150,确实是150
        // tfc+中锻造初值在70到120之间,tng是40到113之间
        // net.dries007.tfc.common.recipes.AnvilRecipe中computeTarget()函数显示锻造初值范围是40-113
        // 锻造指针在0到150之间
        // 这个函数是预计算到某个数值需要各种锻造步骤的数量, 结果保存在steps这个哈希map中
        // 为了降低算法的复杂性, 输入的值是目标锻造值-当前锻造值-锻造偏移值,其中目标锻造值与种子有关,范围是40-113,
        // 当前锻造值是砧gui槽下面的指针,范围是0-150,锻造偏移值是当前配方要求步骤的数值和,范围是-45到48
        // 因此输入值的实际范围是-158到158
        // 但是因为目标锻造值-锻造偏移值一定处于0-150之间,因此实际范围是-150到150,而不是-158到158
        // dp[150+i]表示锻造差值为i时的最少锻造步骤数
        // dp[150]=0
        // dp[150+i]=Math.min(dp[80+i-operations[j]]) +1
        for (int i = -150; i <= 150; i++) {
            steps.put(i, new int[]{0, 0, 0, 0, 0, 0, 0, 0});
        }
        int[] dp = new int[301];
        for (int i = 0; i < 301; i++) {
            dp[i] = 100;
        }
        dp[150] = 0;
        // 这里正反两次遍历dp数组,因为单次遍历有某些值无法到达
        for (int i = 150; i >= -150; i--) {
            foreachoperation(dp, i);
        }
        for (int i = -150; i <= 150; i++) {
            foreachoperation(dp, i);
        }
        operationsTfc.put(0, 3);
        operationsTfc.put(1, 3);
        operationsTfc.put(2, 3);
        operationsTfc.put(3, 3);
        operationsTfc.put(4, 3);

        operationsTfc.put(5, 0);
        operationsTfc.put(6, 0);
        operationsTfc.put(7, 0);
        operationsTfc.put(8, 0);
        operationsTfc.put(9, 0);

        operationsTfc.put(10, 4);
        operationsTfc.put(11, 4);
        operationsTfc.put(12, 4);
        operationsTfc.put(13, 4);
        operationsTfc.put(14, 4);

        operationsTfc.put(15, 5);
        operationsTfc.put(16, 5);
        operationsTfc.put(17, 5);
        operationsTfc.put(18, 5);
        operationsTfc.put(19, 5);

        operationsTfc.put(20, 6);
        operationsTfc.put(21, 6);
        operationsTfc.put(22, 6);
        operationsTfc.put(23, 6);
        operationsTfc.put(24, 6);

        operationsTfc.put(25, 7);
        operationsTfc.put(26, 7);
        operationsTfc.put(27, 7);
        operationsTfc.put(28, 7);
        operationsTfc.put(29, 7);

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
            if (i - op >= -150 && i - op <= 150) {
                if (dp[150 + i] > dp[150 + i - op] + 1) {
                    dp[150 + i] = dp[150 + i - op] + 1;
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
