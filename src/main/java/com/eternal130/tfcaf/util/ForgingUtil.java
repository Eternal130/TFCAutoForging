package com.eternal130.tfcaf.util;

import com.eternal130.tfcaf.Util;
import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.dries007.tfc.common.capabilities.forge.ForgeSteps;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Arrays;

public class ForgingUtil {
    public static final byte[] operations = {-15, -9, -6, -3, 2, 7, 13, 16, 0}; // 0 是为了解决没法打出完美的工具新加入的
    private static final VarHandle TYPE_HANDLE;

    static {
        try {
            Field f = ForgeRule.class.getDeclaredField("type");
            f.setAccessible(true);
            TYPE_HANDLE = MethodHandles.privateLookupIn(ForgeRule.class, MethodHandles.lookup()).unreflectVarHandle(f);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize VarHandle", e);
        }
    }
    /**
     * 从锻造配方的规则列表中提取出最后三步对应的操作类型索引。<br><br>
     * 该方法将 {@link ForgeRule} 数组转换为长度为 3 的整数数组，
     * 分别代表倒数第三步、倒数第二步和最后一步在 {@link ForgingUtil#operations} 中的索引值。<br>
     * @param rules 锻造配方的规则列表，不为 null。
     * @return 长度为 3 的 int 数组，<br>
     *         索引 0：倒数第三步的操作索引<br>
     *         索引 1：倒数第二步的操作索引<br>
     *         索引 2：最后一步的操作索引<br>
     *         所有索引值均在 {@link ForgingUtil#operations} 的有效范围内。
     */
    public static int[] getRules(ForgeRule[] rules) {
        // 相对于1.7版本,没有any类型,每种步骤也只有五种位置,少了LastTwo这种类型,因此少遍历一次
        int[] lastOperations = new int[3];
        // 将锻造要求初始化为-1,表示没有要求
        Arrays.fill(lastOperations, -1);
        // 标志该位置要求是否已经被填充
        boolean[] flag = new boolean[3];
        // 首先遍历一次锻造目标,将确定位置的步骤填充到lastOperations中,例如Hit_Last,Hit_Second_Last,Hit_Third_Last
        // 因为hit的last和notlast相比于其他步骤是反序的,因此单独摘出来判断
        for (ForgeRule rule : rules) {
            // 这三种序号对5取余后分别是1,3,4,Hit_Last的序号是2,Hit_Not_Last的序号是1,单独摘出来判断
            if ((rule.ordinal() != 1 && rule.ordinal() % 5 == 1 && !flag[2]) || rule.ordinal() == 2) {
                lastOperations[2] = ForgingUtil.getStepIndex(rule);
                flag[2] = true;
            } else if (rule.ordinal() % 5 == 3 && !flag[1]) {
                lastOperations[1] = ForgingUtil.getStepIndex(rule);
                flag[1] = true;
            } else if (rule.ordinal() % 5 == 4 && !flag[0]) {
                lastOperations[0] = ForgingUtil.getStepIndex(rule);
                flag[0] = true;
            }
        }
        // 第二次遍历,填充可以位于倒数第二步和倒数第三步的步骤,例如Hit_Not_Last
        // 其他步骤的Not_last序号对5取余后是2，Hit_Not_Last的序号是1，因此单独摘出来判断
        for (ForgeRule rule : rules) {
            // 这一步的序号对5取余后是2
            if ((rule.ordinal() != 2 && rule.ordinal() % 5 == 2) || rule.ordinal() == 1){
                // NOT_LAST 不能是最后一步，只能落在倒数第二步或倒数第三步
                if (flag[1]) {
                    lastOperations[0] = ForgingUtil.getStepIndex(rule);
                    flag[0] = true;
                } else {
                    lastOperations[1] = ForgingUtil.getStepIndex(rule);
                    flag[1] = true;
                }
            }
        }
        // 最后一次遍历,填充剩余的步骤,这里的步骤是可以位于任意位置的步骤,例如BendAny
        for (ForgeRule rule : rules) {
            // 这个步骤的序号对5取余后是0
            if (rule.ordinal() % 5 == 0) {
                // 遍历lastOperations,如果有空位就填充,并且因为锻造需求里每步出现一次,所以只填充一次,跳出大循环
                for (int i = 0; i < 3; i++) {
                    if (!flag[i]) {
                        lastOperations[i] = 8; // Any规则无固定操作,统一用8占位(增量0),不能填-1否则operations越界
                        flag[i] = true;
                        break;
                    }
                }
            }
        }
        // 如果还有空位,说明锻造需求不满3个,这时将空位填入8,对应锻造数值是0(占位),此时未遍历的需求只有Any,对于Any,填入的数值依然是8
        for (int i = 0; i < 3; i++) {
            if (!flag[i]) {
                lastOperations[i] = 8;
            }
        }
        return lastOperations;
    }

    public static ForgeStep getStep(ForgeRule rule) {
        return (ForgeStep) TYPE_HANDLE.get(rule);
    }

    public static int getStepIndex(ForgeRule rule) {
        return indexOfStep(getStep(rule));
    }

    public static int indexOfStep(ForgeStep step) {
        if (step == null) {
            return -1;
        }
        int value = step.step();
        for (int i = 0; i < operations.length; i++) {
            if (operations[i] == value) {
                return i;
            }
        }
        return -1; // 理论上不会发生
    }

    /**
     * 判断即将执行的这步是否为本件的最后一步。
     */
    public static boolean isFinalStep(int difference, int[] lastRules, ForgeSteps itemRules) {
        ForgeStep last = itemRules.last();
        ForgeStep secondLast = itemRules.secondLast();
        boolean hasS = lastRules[1] != 8; // 倒数第二步是否为固定要求(8为自由位占位)
        boolean hasT = lastRules[0] != 8; // 倒数第三步是否为固定要求(8为自由位占位)
        // difference已扣除最后三步固定增量(自由位增量0),加回最后两步增量后为0,即当前只差最后一步
        int tail = (hasS ? operations[lastRules[1]] : 0) + (hasT ? operations[lastRules[0]] : 0);
        return difference + tail == 0
                && last != null && secondLast != null
                && (!hasS || Util.buttonMapping[last.ordinal()] == lastRules[1])
                && (!hasT || Util.buttonMapping[secondLast.ordinal()] == lastRules[0]);
    }
}
