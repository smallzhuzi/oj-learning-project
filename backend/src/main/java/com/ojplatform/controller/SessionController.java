package com.ojplatform.controller;

import com.ojplatform.common.Result;
import com.ojplatform.dto.CreateSessionDTO;
import com.ojplatform.dto.SessionChainDTO;
import com.ojplatform.dto.SessionTrackItemDTO;
import com.ojplatform.entity.PracticeSession;
import com.ojplatform.entity.SessionProblem;
import com.ojplatform.service.PracticeSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 练习会话控制器
 * 管理练习会话的生命周期和题目跳转轨迹
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private PracticeSessionService sessionService;

    /**
     * 创建或复用练习会话（find-or-create）
     * 如果该题目已有轨迹链则复用，否则新建
     *
     * POST /api/sessions
     */
    @PostMapping
    public Result<PracticeSession> create(@Valid @RequestBody CreateSessionDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        PracticeSession session = sessionService.createSession(dto);
        return Result.ok(session);
    }

    /**
     * 获取会话详情
     *
     * GET /api/sessions/{id}
     */
    @GetMapping("/{id}")
    public Result<PracticeSession> detail(@PathVariable Long id) {
        PracticeSession session = sessionService.getById(id);
        if (session == null) {
            return Result.error(404, "会话不存在");
        }
        return Result.ok(session);
    }

    /**
     * 获取会话内的题目跳转轨迹（含题目详情）
     *
     * GET /api/sessions/{id}/track
     */
    @GetMapping("/{id}/track")
    public Result<List<SessionTrackItemDTO>> track(@PathVariable Long id) {
        List<SessionTrackItemDTO> track = sessionService.getSessionTrack(id);
        return Result.ok(track);
    }

    /**
     * 在会话中添加下一题（Dify 推荐跳转）
     *
     * POST /api/sessions/{id}/next?problemSlug=two-sum
     */
    @PostMapping("/{id}/next")
    public Result<SessionProblem> addNext(
            @PathVariable Long id,
            @RequestParam String problemSlug,
            @RequestParam(defaultValue = "leetcode") String ojPlatform) {
        SessionProblem sp = sessionService.addNextProblem(id, problemSlug, ojPlatform);
        return Result.ok(sp);
    }

    /**
     * 绑定 Dify 对话 ID 到会话
     *
     * PUT /api/sessions/{id}/dify?conversationId=xxx
     */
    @PutMapping("/{id}/dify")
    public Result<Void> bindDify(
            @PathVariable Long id,
            @RequestParam String conversationId) {
        sessionService.bindDifyConversation(id, conversationId);
        return Result.ok();
    }

    /**
     * 获取当前用户所有轨迹链
     *
     * GET /api/sessions/chains
     */
    @GetMapping("/chains")
    public Result<List<SessionChainDTO>> chains(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<SessionChainDTO> chains = sessionService.getUserChains(userId);
        return Result.ok(chains);
    }

    /**
     * 触发会话活跃（切换到该链时调用，更新排序）
     *
     * PUT /api/sessions/{id}/touch
     */
    @PutMapping("/{id}/touch")
    public Result<Void> touch(@PathVariable Long id) {
        sessionService.touchSession(id);
        return Result.ok();
    }

    /**
     * 删除轨迹链（级联删除关联题目记录）
     *
     * DELETE /api/sessions/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteChain(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        sessionService.deleteChain(id, userId);
        return Result.ok();
    }
}
