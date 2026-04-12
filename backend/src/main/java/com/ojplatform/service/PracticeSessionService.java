package com.ojplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ojplatform.dto.CreateSessionDTO;
import com.ojplatform.dto.SessionChainDTO;
import com.ojplatform.dto.SessionTrackItemDTO;
import com.ojplatform.entity.PracticeSession;
import com.ojplatform.entity.SessionProblem;

import java.util.List;

/**
 * 练习会话服务接口
 */
public interface PracticeSessionService extends IService<PracticeSession> {

    /**
     * 创建或复用练习会话（find-or-create）
     * 如果用户已有以该题目为头题的轨迹链，直接返回已有会话
     */
    PracticeSession createSession(CreateSessionDTO dto);

    /**
     * 在会话中添加下一题（AI 推荐跳转）
     */
    SessionProblem addNextProblem(Long sessionId, String problemSlug, String ojPlatform);

    /**
     * 获取会话内的题目轨迹（含题目详情：slug、标题、题号、难度）
     */
    List<SessionTrackItemDTO> getSessionTrack(Long sessionId);

    /**
     * 绑定 Dify 对话 ID 到会话
     */
    void bindDifyConversation(Long sessionId, String difyConversationId);

    /**
     * 获取用户所有轨迹链概要
     */
    List<SessionChainDTO> getUserChains(Long userId);

    /**
     * 删除轨迹链（级联删除关联的 session_problems）
     */
    void deleteChain(Long sessionId, Long userId);

    /**
     * 触发会话活跃（更新 last_active_at，用于排序）
     */
    void touchSession(Long sessionId);
}
