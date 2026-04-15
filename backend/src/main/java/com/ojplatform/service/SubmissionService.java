package com.ojplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ojplatform.dto.SubmitCodeDTO;
import com.ojplatform.dto.UserStatsDTO;
import com.ojplatform.entity.Submission;

import java.util.List;
import java.util.Map;

/**
 * 提交相关业务接口。
 */
public interface SubmissionService extends IService<Submission> {

    /**
     * 提交代码到远程 OJ 并创建提交记录
     */
    Submission submitCode(SubmitCodeDTO dto);

    /**
     * 轮询远程 OJ 获取判题结果，更新本地记录
     */
    Submission pollResult(Long submissionId);

    /**
     * 查询指定会话内的所有提交记录（按提交时间升序）
     */
    List<Submission> getSessionSubmissions(Long sessionId);

    /**
     * 查询某用户在某题上的所有提交记录（按提交时间倒序）
     */
    List<Submission> getUserProblemSubmissions(Long userId, String problemSlug, String ojPlatform);

    /**
     * 批量查询用户所有已提交题目的状态摘要
     * @return slug → "accepted" / "attempted"
     */
    Map<String, String> getUserStatusMap(Long userId);

    /**
     * 获取用户做题统计（按平台、难度、近期每日）
     */
    UserStatsDTO getUserStats(Long userId);
}
