package com.ojplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ojplatform.dto.SessionTrackItemDTO;
import com.ojplatform.entity.SessionProblem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会话题目关联表 Mapper 接口
 */
public interface SessionProblemMapper extends BaseMapper<SessionProblem> {

    /**
     * 查询会话轨迹（关联题目详情）
     * 一次 JOIN 查询，返回 slug、title、frontendId、difficulty
     */
    @Select("SELECT sp.id, sp.session_id, sp.problem_id, sp.jump_type, sp.seq_order, sp.jumped_at, " +
            "p.slug, p.title, p.frontend_id, p.difficulty, " +
            "(SELECT COUNT(*) FROM submissions s WHERE s.session_id = sp.session_id AND s.problem_id = sp.problem_id AND s.status != 'Pending') AS attempt_count, " +
            "(SELECT COUNT(*) > 0 FROM submissions s WHERE s.session_id = sp.session_id AND s.problem_id = sp.problem_id AND s.status = 'Accepted') AS accepted " +
            "FROM session_problems sp " +
            "LEFT JOIN problems p ON sp.problem_id = p.id " +
            "WHERE sp.session_id = #{sessionId} " +
            "ORDER BY sp.seq_order ASC")
    List<SessionTrackItemDTO> selectTrackWithProblemInfo(@Param("sessionId") Long sessionId);
}
