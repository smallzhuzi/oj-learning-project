package com.ojplatform.service;

import com.ojplatform.dto.LeetCodeProblemDetail;
import com.ojplatform.entity.Problem;

import java.util.List;

/**
 * 标签同步相关业务接口。
 */
public interface TagSyncService {

    /**
     * 将题目的原始平台标签同步到：
     * 1. 平台标签映射表 platform_tags
     * 2. 统一标签表 tags
     * 3. 题目标签关系表 problem_tag_relations
     */
    void syncProblemTags(Problem problem, List<LeetCodeProblemDetail.TopicTag> rawTags, String ojPlatform);
}
