package com.ojplatform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ojplatform.dto.ProblemTagOptionDTO;
import com.ojplatform.dto.ProblemQueryDTO;
import com.ojplatform.entity.Problem;

/**
 * 题目相关业务接口。
 */
public interface ProblemService extends IService<Problem> {

    /**
     * 分页查询题目列表（支持关键词搜索 + 难度筛选）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<Problem> queryProblems(ProblemQueryDTO queryDTO);

    /**
     * 根据 slug 获取题目详情，若本地缓存不存在则从远程 OJ 拉取
     *
     * @param slug       题目 slug
     * @param ojPlatform OJ 平台标识
     * @return 题目实体
     */
    Problem getBySlug(String slug, String ojPlatform);

    /**
     * 搜索题库可选标签
     */
    Page<ProblemTagOptionDTO> searchTagOptions(String ojPlatform, String keyword, long pageNum, long pageSize);
}
