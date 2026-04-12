package com.ojplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ojplatform.dto.ProblemStatusDTO;
import com.ojplatform.entity.Submission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 提交记录表 Mapper 接口
 */
public interface SubmissionMapper extends BaseMapper<Submission> {

    /**
     * 批量查询用户所有已提交题目的状态摘要
     * 返回 slug → accepted/attempted
     */
    @Select("SELECT p.slug, " +
            "CASE WHEN SUM(CASE WHEN s.status = 'Accepted' THEN 1 ELSE 0 END) > 0 " +
            "     THEN 'accepted' ELSE 'attempted' END AS status " +
            "FROM submissions s " +
            "JOIN problems p ON s.problem_id = p.id " +
            "WHERE s.user_id = #{userId} AND s.status != 'Pending' " +
            "GROUP BY p.slug")
    List<ProblemStatusDTO> selectUserStatusMap(@Param("userId") Long userId);

    /**
     * 按平台统计：提交数 + 通过的不同题目数
     * 返回 [{platform, submitted, solved}]
     */
    @Select("SELECT p.oj_platform AS platform, " +
            "  COUNT(*) AS submitted, " +
            "  COUNT(DISTINCT CASE WHEN s.status = 'Accepted' THEN p.id END) AS solved " +
            "FROM submissions s " +
            "JOIN problems p ON s.problem_id = p.id " +
            "WHERE s.user_id = #{userId} AND s.status != 'Pending' " +
            "GROUP BY p.oj_platform")
    List<Map<String, Object>> selectStatsByPlatform(@Param("userId") Long userId);

    /**
     * 按平台+难度统计通过的不同题目数
     * 返回 [{platform, difficulty, solved}]
     */
    @Select("SELECT p.oj_platform AS platform, p.difficulty, " +
            "  COUNT(DISTINCT p.id) AS solved " +
            "FROM submissions s " +
            "JOIN problems p ON s.problem_id = p.id " +
            "WHERE s.user_id = #{userId} AND s.status = 'Accepted' " +
            "GROUP BY p.oj_platform, p.difficulty")
    List<Map<String, Object>> selectSolvedByDifficulty(@Param("userId") Long userId);

    /**
     * 近 30 天每日提交次数
     * 返回 [{date, count}]
     */
    @Select("SELECT DATE(s.submitted_at) AS date, COUNT(*) AS count " +
            "FROM submissions s " +
            "WHERE s.user_id = #{userId} " +
            "  AND s.submitted_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "  AND s.status != 'Pending' " +
            "GROUP BY DATE(s.submitted_at) " +
            "ORDER BY date")
    List<Map<String, Object>> selectRecentDaily(@Param("userId") Long userId);
}
