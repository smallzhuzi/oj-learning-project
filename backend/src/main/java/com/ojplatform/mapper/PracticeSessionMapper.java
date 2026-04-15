package com.ojplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ojplatform.dto.SessionChainDTO;
import com.ojplatform.entity.PracticeSession;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 练习会话数据访问接口。
 */
public interface PracticeSessionMapper extends BaseMapper<PracticeSession> {

    /**
     * 获取用户所有轨迹链概要（含头题信息和题目数量）
     */
    @Select("SELECT ps.id AS session_id, ps.dify_conversation_id, ps.started_at, " +
            "p.slug AS head_slug, p.title AS head_title, p.frontend_id AS head_frontend_id, p.difficulty AS head_difficulty, " +
            "(SELECT COUNT(*) FROM session_problems WHERE session_id = ps.id) AS problem_count " +
            "FROM practice_sessions ps " +
            "LEFT JOIN session_problems sp ON sp.session_id = ps.id AND sp.jump_type = 'initial' " +
            "LEFT JOIN problems p ON sp.problem_id = p.id " +
            "WHERE ps.user_id = #{userId} " +
            "ORDER BY ps.last_active_at DESC")
    List<SessionChainDTO> selectUserChains(@Param("userId") Long userId);

    /**
     * 按用户和头题 slug 查找已有轨迹链（find-or-create 用）
     */
    @Select("SELECT ps.* FROM practice_sessions ps " +
            "INNER JOIN session_problems sp ON sp.session_id = ps.id " +
            "INNER JOIN problems p ON sp.problem_id = p.id " +
            "WHERE ps.user_id = #{userId} AND p.slug = #{problemSlug} AND sp.jump_type = 'initial' " +
            "LIMIT 1")
    PracticeSession selectByUserAndHeadSlug(@Param("userId") Long userId, @Param("problemSlug") String problemSlug);
}
