package com.ojplatform.service;

import com.ojplatform.dto.ProblemTagDTO;
import com.ojplatform.entity.Problem;

import java.util.List;

/**
 * 题目标签统一组装服务
 */
public interface ProblemTagFacadeService {

    /**
     * 获取题目的统一标签列表
     * 优先使用新标签关系表；若暂无映射数据，则从 problems.topic_tags 回退解析。
     */
    List<ProblemTagDTO> getUnifiedTags(Problem problem);
}
