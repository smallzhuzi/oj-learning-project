package com.ojplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ojplatform.entity.ProblemSetItem;
import com.ojplatform.dto.ProblemSetItemDetailDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 题单条目数据访问接口。
 */
public interface ProblemSetItemMapper extends BaseMapper<ProblemSetItem> {

    /**
     * 查询题单内所有题目的详细信息（含题目基本信息）
     */
    @Select("SELECT psi.id, psi.set_id, psi.problem_id, psi.seq_order, psi.score, " +
            "p.slug, p.title, p.frontend_id, p.difficulty, p.acceptance_rate, p.topic_tags " +
            "FROM problem_set_items psi " +
            "JOIN problems p ON psi.problem_id = p.id " +
            "WHERE psi.set_id = #{setId} " +
            "ORDER BY psi.seq_order ASC")
    List<ProblemSetItemDetailDTO> selectItemsWithDetail(@Param("setId") Long setId);
}
