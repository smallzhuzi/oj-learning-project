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
 * 会话相关接口控制器。
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private PracticeSessionService sessionService;

/**
 * 创建练习会话。
 */
    @PostMapping
    public Result<PracticeSession> create(@Valid @RequestBody CreateSessionDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        PracticeSession session = sessionService.createSession(dto);
        return Result.ok(session);
    }

/**
 * 查询练习会话详情。
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
 * 查询会话做题轨迹。
 */
    @GetMapping("/{id}/track")
    public Result<List<SessionTrackItemDTO>> track(@PathVariable Long id) {
        List<SessionTrackItemDTO> track = sessionService.getSessionTrack(id);
        return Result.ok(track);
    }

/**
 * 向会话追加下一题。
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
 * 绑定 Dify 会话标识。
 */
    @PutMapping("/{id}/dify")
    public Result<Void> bindDify(
            @PathVariable Long id,
            @RequestParam String conversationId) {
        sessionService.bindDifyConversation(id, conversationId);
        return Result.ok();
    }

/**
 * 查询当前用户的会话链路。
 */
    @GetMapping("/chains")
    public Result<List<SessionChainDTO>> chains(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<SessionChainDTO> chains = sessionService.getUserChains(userId);
        return Result.ok(chains);
    }

/**
 * 刷新会话最近访问时间。
 */
    @PutMapping("/{id}/touch")
    public Result<Void> touch(@PathVariable Long id) {
        sessionService.touchSession(id);
        return Result.ok();
    }

/**
 * 删除会话链路。
 */
    @DeleteMapping("/{id}")
    public Result<Void> deleteChain(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        sessionService.deleteChain(id, userId);
        return Result.ok();
    }
}
