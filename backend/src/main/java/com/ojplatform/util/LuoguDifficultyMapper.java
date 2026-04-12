package com.ojplatform.util;

import java.util.Map;

/**
 * 洛谷难度映射工具
 * 将洛谷的数字难度等级（0-7）转为中文标签，直接存储原始难度
 */
public class LuoguDifficultyMapper {

    /** 洛谷难度值 → 中文标签 */
    private static final Map<Integer, String> LABELS = Map.of(
            0, "暂无评定",
            1, "入门",
            2, "普及-",
            3, "普及/提高-",
            4, "普及+/提高",
            5, "提高+/省选-",
            6, "省选/NOI-",
            7, "NOI/NOI+/CTSC"
    );

    /**
     * 将洛谷数字难度转为中文标签
     */
    public static String toLabel(int luoguDifficulty) {
        return LABELS.getOrDefault(luoguDifficulty, "暂无评定");
    }

    private LuoguDifficultyMapper() {}
}
