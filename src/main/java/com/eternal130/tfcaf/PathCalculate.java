package com.eternal130.tfcaf;

import com.eternal130.tfcaf.util.ForgingUtil;
import net.dries007.tfc.common.capabilities.forge.ForgeSteps;

import java.io.InputStream;

public final class PathCalculate {
    private static final byte[] NEXT_STEPS = new byte[301];
    static {
        // 这个steps.bin由laolizhennui/TFCForgingStepGenerator项目生成
        try (InputStream is = PathCalculate.class.getResourceAsStream("/steps.bin")) {
            if (is == null) {
                throw new RuntimeException("数据文件 steps.bin 未找到！");
            }
            byte[] data = is.readAllBytes();
            if (data.length != 301) {
                throw new RuntimeException("数据大小应为 301 字节，实际为 " + data.length);
            }
            System.arraycopy(data, 0, NEXT_STEPS, 0, 301);
        } catch (Exception e) {
            TFCAutoForging.LOGGER.error("加载数据文件 steps.bin 时出现问题", e);
        }
    }

    /**
     * 根据差值 diff（范围 -150 ~ 150）返回应执行的操作索引（0~7）。
     * diff==0 时表中存的是 8(占位),正常会被收尾阶段拦截;越界视为不可达,返回 -1 供调用方中止连发。
     */
    public static int getNextStep(int diff) {
        int idx = diff + 150;
        if (idx < 0 || idx >= NEXT_STEPS.length) {
            return -1;
        }
        return NEXT_STEPS[idx];
    }

    /**
     * 计算下一步锻造操作索引。
     *
     * @param targetPoint  目标锻造值
     * @param currentPoint 当前锻造值
     * @param lastRules    配方要求的最后三步操作索引（倒数第三、倒数第二、最后）
     * @param itemRules    当前已执行的历史步骤（最近三次）
     * @return 下一步操作索引（0~7），若无法完成则返回 -1
     */
    public static int nextOperationOffset(int targetPoint, int currentPoint, int[] lastRules, ForgeSteps itemRules) {
//        TFCAutoForging.LOGGER.info("targetPoint: {}, currentPoint: {}", targetPoint, currentPoint);
        int f = ForgingUtil.operations[lastRules[2]]; // 最后一步的增量(配方必有)
        int s = ForgingUtil.operations[lastRules[1]];
        int t = ForgingUtil.operations[lastRules[0]];
        boolean hasS = lastRules[1] != 8;
        boolean hasT = lastRules[0] != 8;
        
        // 获取最近两次历史步骤的操作索引（用于收尾匹配）
        int lastIdx = ForgingUtil.indexOfStep(itemRules.last());
        int secondLastIdx = ForgingUtil.indexOfStep(itemRules.secondLast());

        // ---------- 收尾阶段判定（按顺序从短到长） ----------
        // 1) 只差最后一步
        if (currentPoint + f == targetPoint
                && (!hasS || lastIdx == lastRules[1])
                && (!hasT || secondLastIdx == lastRules[0])) {
            return lastRules[2];
        }

        // 2) 只差最后两步
        if (hasS && currentPoint + f + s == targetPoint
                && (!hasT || lastIdx == lastRules[0])) {
            return lastRules[1];
        }

        // 3) 只差最后三步（仅倒数第三步有固定要求时,否则会返回8空操作导致发包崩溃）
        if (hasT && currentPoint + f + s + t == targetPoint) {
            return lastRules[0];
        }

        // ---------- 非收尾阶段：查预计算表 ----------
        int diff = targetPoint - currentPoint - (f + s + t);
        return getNextStep(diff);
    }
}
