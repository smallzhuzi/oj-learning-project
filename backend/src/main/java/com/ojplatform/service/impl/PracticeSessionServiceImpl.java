package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ojplatform.dto.CreateSessionDTO;
import com.ojplatform.dto.SessionChainDTO;
import com.ojplatform.dto.SessionTrackItemDTO;
import com.ojplatform.entity.PracticeSession;
import com.ojplatform.entity.Problem;
import com.ojplatform.entity.SessionProblem;
import com.ojplatform.mapper.PracticeSessionMapper;
import com.ojplatform.mapper.SessionProblemMapper;
import com.ojplatform.service.PracticeSessionService;
import com.ojplatform.service.ProblemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 练习会话相关业务实现。
 */
@Service
public class PracticeSessionServiceImpl
        extends ServiceImpl<PracticeSessionMapper, PracticeSession>
        implements PracticeSessionService {

    private static final Logger log = LoggerFactory.getLogger(PracticeSessionServiceImpl.class);

    @Autowired
    private SessionProblemMapper sessionProblemMapper;

    @Autowired
    private ProblemService problemService;

/**
 * 创建新的练习会话。
 */
    @Override
    @Transactional
    public PracticeSession createSession(CreateSessionDTO dto) {
        // 1. 查找该用户是否已有以此题为头题的轨迹链
        PracticeSession existing = baseMapper.selectByUserAndHeadSlug(dto.getUserId(), dto.getProblemSlug());
        if (existing != null) {
            log.info("复用已有轨迹链：sessionId={}, headSlug={}", existing.getId(), dto.getProblemSlug());
            // 更新活跃时间
            existing.setLastActiveAt(java.time.LocalDateTime.now());
            baseMapper.updateById(existing);
            return existing;
        }

        // 2. 不存在则创建新会话
        PracticeSession session = new PracticeSession();
        session.setUserId(dto.getUserId());
        baseMapper.insert(session);

        // 3. 查找初始题目
        String platform = dto.getOjPlatform() != null ? dto.getOjPlatform() : "leetcode";
        Problem problem = problemService.getBySlug(dto.getProblemSlug(), platform);
        if (problem == null) {
            throw new RuntimeException("题目不存在：" + dto.getProblemSlug());
        }

        // 4. 记录初始题目到会话轨迹
        SessionProblem sp = new SessionProblem();
        sp.setSessionId(session.getId());
        sp.setProblemId(problem.getId());
        sp.setJumpType("initial");
        sp.setSeqOrder(1);
        sessionProblemMapper.insert(sp);

        log.info("创建新轨迹链：sessionId={}, headSlug={}", session.getId(), dto.getProblemSlug());
        return session;
    }

/**
 * 向练习会话追加下一题。
 */
    @Override
    @Transactional
    public SessionProblem addNextProblem(Long sessionId, String problemSlug, String ojPlatform) {
        String platform = ojPlatform != null ? ojPlatform : "leetcode";
        Problem problem = problemService.getBySlug(problemSlug, platform);
        if (problem == null) {
            throw new RuntimeException("推荐题目不存在：" + problemSlug);
        }

        Long count = sessionProblemMapper.selectCount(
                new LambdaQueryWrapper<SessionProblem>()
                        .eq(SessionProblem::getSessionId, sessionId)
        );

        SessionProblem sp = new SessionProblem();
        sp.setSessionId(sessionId);
        sp.setProblemId(problem.getId());
        sp.setJumpType("next_recommend");
        sp.setSeqOrder(count.intValue() + 1);
        sessionProblemMapper.insert(sp);

        // 更新会话活跃时间
        PracticeSession session = baseMapper.selectById(sessionId);
        if (session != null) {
            session.setLastActiveAt(java.time.LocalDateTime.now());
            baseMapper.updateById(session);
        }

        return sp;
    }

/**
 * 读取会话做题轨迹。
 */
    @Override
    public List<SessionTrackItemDTO> getSessionTrack(Long sessionId) {
        return sessionProblemMapper.selectTrackWithProblemInfo(sessionId);
    }

/**
 * 绑定 Dify 会话标识。
 */
    @Override
    public void bindDifyConversation(Long sessionId, String difyConversationId) {
        PracticeSession session = baseMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在：" + sessionId);
        }
        session.setDifyConversationId(difyConversationId);
        baseMapper.updateById(session);
    }

/**
 * 查询用户的会话链路列表。
 */
    @Override
    public List<SessionChainDTO> getUserChains(Long userId) {
        return baseMapper.selectUserChains(userId);
    }

/**
 * 删除指定会话链路。
 */
    @Override
    @Transactional
    public void deleteChain(Long sessionId, Long userId) {
        // 校验：确保是该用户的会话
        PracticeSession session = baseMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new RuntimeException("会话不存在或无权删除");
        }

        // 先删关联的题目记录，再删会话本身
        sessionProblemMapper.delete(
                new LambdaQueryWrapper<SessionProblem>()
                        .eq(SessionProblem::getSessionId, sessionId)
        );
        baseMapper.deleteById(sessionId);

        log.info("删除轨迹链：sessionId={}, userId={}", sessionId, userId);
    }

/**
 * 刷新会话最近活动时间。
 */
    @Override
    public void touchSession(Long sessionId) {
        PracticeSession session = baseMapper.selectById(sessionId);
        if (session != null) {
            session.setLastActiveAt(java.time.LocalDateTime.now());
            baseMapper.updateById(session);
        }
    }
}
