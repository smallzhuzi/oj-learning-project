package com.ojplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ojplatform.dto.ProblemTagDTO;
import com.ojplatform.dto.ProblemTagOptionDTO;
import com.ojplatform.entity.ProblemTagRelation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 题目标签关联 Mapper 接口
 */
public interface ProblemTagRelationMapper extends BaseMapper<ProblemTagRelation> {

    @Select("""
            SELECT
                t.id AS id,
                t.tag_key AS `key`,
                t.display_name AS label,
                tt.type_key AS type,
                pt.source_name AS sourceName,
                pt.source_slug AS sourceSlug,
                pt.oj_platform AS ojPlatform
            FROM problem_tag_relations ptr
            LEFT JOIN tags t ON ptr.tag_id = t.id
            LEFT JOIN tag_types tt ON t.tag_type_id = tt.id
            LEFT JOIN platform_tags pt ON ptr.platform_tag_id = pt.id
            WHERE ptr.problem_id = #{problemId}
            ORDER BY t.sort_order ASC, t.id ASC, ptr.id ASC
            """)
    List<ProblemTagDTO> selectUnifiedTagsByProblemId(@Param("problemId") Long problemId);

    @Select("""
            <script>
            SELECT DISTINCT ptr.problem_id
            FROM problem_tag_relations ptr
            LEFT JOIN tags t ON ptr.tag_id = t.id
            LEFT JOIN platform_tags pt ON ptr.platform_tag_id = pt.id
            WHERE pt.oj_platform = #{ojPlatform}
              AND (
                LOWER(COALESCE(t.tag_key, '')) IN
                <foreach collection="tags" item="tag" open="(" separator="," close=")">
                    #{tag}
                </foreach>
                OR LOWER(COALESCE(t.display_name, '')) IN
                <foreach collection="tags" item="tag" open="(" separator="," close=")">
                    #{tag}
                </foreach>
                OR LOWER(COALESCE(pt.source_name, '')) IN
                <foreach collection="tags" item="tag" open="(" separator="," close=")">
                    #{tag}
                </foreach>
                OR LOWER(COALESCE(pt.source_slug, '')) IN
                <foreach collection="tags" item="tag" open="(" separator="," close=")">
                    #{tag}
                </foreach>
              )
            </script>
            """)
    List<Long> selectProblemIdsByPlatformAndTags(@Param("ojPlatform") String ojPlatform,
                                                 @Param("tags") List<String> tags);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM (
                SELECT t.id
                FROM problem_tag_relations ptr
                JOIN platform_tags pt ON ptr.platform_tag_id = pt.id
                JOIN tags t ON ptr.tag_id = t.id
                WHERE pt.oj_platform = #{ojPlatform}
                <if test="keyword != null and keyword != ''">
                  AND (
                    LOWER(COALESCE(t.display_name, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                    OR LOWER(COALESCE(t.tag_key, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                    OR LOWER(COALESCE(pt.source_name, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                    OR LOWER(COALESCE(pt.source_slug, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                  )
                </if>
                GROUP BY t.id
            ) tmp
            </script>
            """)
    long countTagOptions(@Param("ojPlatform") String ojPlatform,
                         @Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT
                MIN(t.tag_key) AS `key`,
                MIN(t.display_name) AS label,
                MIN(tt.type_key) AS type
            FROM problem_tag_relations ptr
            JOIN platform_tags pt ON ptr.platform_tag_id = pt.id
            JOIN tags t ON ptr.tag_id = t.id
            LEFT JOIN tag_types tt ON t.tag_type_id = tt.id
            WHERE pt.oj_platform = #{ojPlatform}
            <if test="keyword != null and keyword != ''">
              AND (
                LOWER(COALESCE(t.display_name, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                OR LOWER(COALESCE(t.tag_key, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                OR LOWER(COALESCE(pt.source_name, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                OR LOWER(COALESCE(pt.source_slug, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
              )
            </if>
            GROUP BY t.id
            ORDER BY MIN(COALESCE(tt.sort_order, 9999)) ASC, MIN(t.sort_order) ASC, MIN(t.display_name) ASC, MIN(t.id) ASC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<ProblemTagOptionDTO> searchTagOptions(@Param("ojPlatform") String ojPlatform,
                                               @Param("keyword") String keyword,
                                               @Param("offset") long offset,
                                               @Param("pageSize") long pageSize);
}
