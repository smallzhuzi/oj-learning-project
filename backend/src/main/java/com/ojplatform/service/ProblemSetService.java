package com.ojplatform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ojplatform.dto.CreateProblemSetDTO;
import com.ojplatform.dto.ProblemSetItemDetailDTO;
import com.ojplatform.dto.QuickGenerateDTO;
import com.ojplatform.entity.ProblemSet;

import java.util.List;

/**
 * 题单相关业务接口。
 */
public interface ProblemSetService extends IService<ProblemSet> {

    /**
     * 创建题单（手动选题）
     */
    ProblemSet createProblemSet(CreateProblemSetDTO dto);

    /**
     * 快速组题（按难度分布随机抽取）
     */
    ProblemSet quickGenerate(QuickGenerateDTO dto);

    /**
     * 查询用户的题单列表（分页）
     */
    IPage<ProblemSet> getUserProblemSets(Long userId, int pageNum, int pageSize);

    /**
     * 查询公开题单列表（分页）
     */
    IPage<ProblemSet> getPublicProblemSets(int pageNum, int pageSize);

    /**
     * 获取题单内所有题目详情
     */
    List<ProblemSetItemDetailDTO> getProblemSetItems(Long setId);

    /**
     * 向题单添加题目
     */
    void addProblemToSet(Long setId, String problemSlug, Integer score, Long userId);

    /**
     * 从题单移除题目
     */
    void removeProblemFromSet(Long setId, Long itemId, Long userId);

    /**
     * 调整题单内题目顺序
     */
    void reorderItems(Long setId, List<Long> itemIds, Long userId);

    /**
     * 删除题单
     */
    void deleteProblemSet(Long setId, Long userId);

    /**
     * 更新题单基本信息
     */
    void updateProblemSet(Long setId, CreateProblemSetDTO dto);
}
