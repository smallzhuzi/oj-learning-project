package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ojplatform.dto.ProblemTagDTO;
import com.ojplatform.dto.UpdateUserProfileDTO;
import com.ojplatform.entity.Problem;
import com.ojplatform.entity.Submission;
import com.ojplatform.entity.UserProfile;
import com.ojplatform.mapper.UserProfileMapper;
import com.ojplatform.service.ProblemTagFacadeService;
import com.ojplatform.service.ProblemService;
import com.ojplatform.service.SubmissionService;
import com.ojplatform.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户资料相关业务实现。
 */
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileServiceImpl.class);

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProblemTagFacadeService problemTagFacadeService;

    @Override
    public UserProfile getOrCreateProfile(Long userId) {
        UserProfile profile = baseMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId)
        );
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setSkillLevel("beginner");
            profile.setSolvedEasy(0);
            profile.setSolvedMedium(0);
            profile.setSolvedHard(0);
            profile.setTotalSubmissions(0);
            profile.setAcceptanceRate(BigDecimal.ZERO);
            baseMapper.insert(profile);
            log.info("创建用户画像：userId={}", userId);
        }
        return profile;
    }

    @Override
    public UserProfile updateSelfAssessment(UpdateUserProfileDTO dto) {
        UserProfile profile = getOrCreateProfile(dto.getUserId());
        if (dto.getSkillLevel() != null) {
            profile.setSkillLevel(dto.getSkillLevel());
        }
        if (dto.getTargetLevel() != null) {
            profile.setTargetLevel(dto.getTargetLevel());
        }
        baseMapper.updateById(profile);
        return profile;
    }

    @Override
    public UserProfile analyzeProfile(Long userId) {
        UserProfile profile = getOrCreateProfile(userId);

        // 1. 查询该用户的所有非 Pending 提交
        List<Submission> allSubmissions = submissionService.list(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getUserId, userId)
                        .ne(Submission::getStatus, "Pending")
        );
        profile.setTotalSubmissions(allSubmissions.size());

        if (allSubmissions.isEmpty()) {
            profile.setLastAnalyzedAt(LocalDateTime.now());
            baseMapper.updateById(profile);
            return profile;
        }

        // 2. 统计通过率
        long acceptedCount = allSubmissions.stream()
                .filter(s -> "Accepted".equals(s.getStatus()))
                .count();
        if (!allSubmissions.isEmpty()) {
            BigDecimal rate = BigDecimal.valueOf(acceptedCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(allSubmissions.size()), 2, RoundingMode.HALF_UP);
            profile.setAcceptanceRate(rate);
        }

        // 3. 统计各难度的 AC 题目数（去重）
        Set<Long> acProblemIds = allSubmissions.stream()
                .filter(s -> "Accepted".equals(s.getStatus()))
                .map(Submission::getProblemId)
                .collect(Collectors.toSet());

        int solvedEasy = 0, solvedMedium = 0, solvedHard = 0;
        Map<String, Integer> tagAcCount = new HashMap<>();
        Map<String, Integer> tagTotalCount = new HashMap<>();

        // 获取所有相关题目
        Set<Long> allProblemIds = allSubmissions.stream()
                .map(Submission::getProblemId)
                .collect(Collectors.toSet());

        for (Long pid : allProblemIds) {
            Problem problem = problemService.getById(pid);
            if (problem == null) continue;

            boolean isAc = acProblemIds.contains(pid);

            // 统计各难度解题数
            if (isAc) {
                switch (problem.getDifficulty()) {
                    case "Easy" -> solvedEasy++;
                    case "Medium" -> solvedMedium++;
                    case "Hard" -> solvedHard++;
                }
            }

            // 统计标签分布（用于擅长/薄弱分析）
            List<ProblemTagDTO> unifiedTags = problemTagFacadeService.getUnifiedTags(problem);
            if (unifiedTags != null && !unifiedTags.isEmpty()) {
                for (ProblemTagDTO tag : unifiedTags) {
                    // 画像统计优先使用展示名，避免洛谷数字标签直接暴露到前端
                    String tagLabel = tag.getLabel();
                    if (tagLabel == null || tagLabel.isBlank()) {
                        tagLabel = tag.getName();
                    }
                    if (tagLabel == null || tagLabel.isBlank()) {
                        continue;
                    }
                    tagTotalCount.merge(tagLabel, 1, Integer::sum);
                    if (isAc) {
                        tagAcCount.merge(tagLabel, 1, Integer::sum);
                    }
                }
            }
        }

        profile.setSolvedEasy(solvedEasy);
        profile.setSolvedMedium(solvedMedium);
        profile.setSolvedHard(solvedHard);

        // 4. 计算擅长和薄弱标签（按通过率排序）
        List<Map.Entry<String, Double>> tagRates = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : tagTotalCount.entrySet()) {
            if (entry.getValue() >= 2) { // 至少做过2题该标签才统计
                double rate = (double) tagAcCount.getOrDefault(entry.getKey(), 0) / entry.getValue();
                tagRates.add(Map.entry(entry.getKey(), rate));
            }
        }
        tagRates.sort(Comparator.comparingDouble(e -> -e.getValue()));

        List<String> strongTags = tagRates.stream()
                .filter(e -> e.getValue() >= 0.6)
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> weakTags = tagRates.stream()
                .filter(e -> e.getValue() < 0.4)
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        try {
            profile.setStrongTags(objectMapper.writeValueAsString(strongTags));
            profile.setWeakTags(objectMapper.writeValueAsString(weakTags));
        } catch (Exception e) {
            log.warn("序列化标签失败", e);
        }

        profile.setLastAnalyzedAt(LocalDateTime.now());
        baseMapper.updateById(profile);
        log.info("用户画像分析完成：userId={}, Easy={}, Medium={}, Hard={}", userId, solvedEasy, solvedMedium, solvedHard);
        return profile;
    }
}
