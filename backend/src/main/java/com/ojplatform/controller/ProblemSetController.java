package com.ojplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ojplatform.common.Result;
import com.ojplatform.dto.CreateProblemSetDTO;
import com.ojplatform.dto.ProblemSetItemDetailDTO;
import com.ojplatform.dto.QuickGenerateDTO;
import com.ojplatform.dto.SmartGenerateDTO;
import com.ojplatform.entity.ProblemSet;
import com.ojplatform.service.ProblemSetService;
import com.ojplatform.service.DifyApiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题单控制器
 * 提供题单 CRUD、快速组题、Dify 智能组题等接口
 */
@RestController
@RequestMapping("/api/problem-sets")
public class ProblemSetController {

    @Autowired
    private ProblemSetService problemSetService;

    /**
     * 创建题单（手动选题）
     * POST /api/problem-sets
     */
    @PostMapping
    public Result<ProblemSet> create(@Valid @RequestBody CreateProblemSetDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        ProblemSet ps = problemSetService.createProblemSet(dto);
        return Result.ok(ps);
    }

    /**
     * 快速组题
     * POST /api/problem-sets/quick-generate
     */
    @PostMapping("/quick-generate")
    public Result<ProblemSet> quickGenerate(@Valid @RequestBody QuickGenerateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        ProblemSet ps = problemSetService.quickGenerate(dto);
        return Result.ok(ps);
    }

    /**
     * 获取我的题单列表
     * GET /api/problem-sets?pageNum=1&pageSize=20
     */
    @GetMapping
    public Result<IPage<ProblemSet>> myList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        IPage<ProblemSet> page = problemSetService.getUserProblemSets(userId, pageNum, pageSize);
        return Result.ok(page);
    }

    /**
     * 获取公开题单列表
     * GET /api/problem-sets/public?pageNum=1&pageSize=20
     */
    @GetMapping("/public")
    public Result<IPage<ProblemSet>> publicList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        IPage<ProblemSet> page = problemSetService.getPublicProblemSets(pageNum, pageSize);
        return Result.ok(page);
    }

    /**
     * 获取题单详情（含题目列表）
     * GET /api/problem-sets/{id}
     */
    @GetMapping("/{id}")
    public Result<ProblemSet> detail(@PathVariable Long id) {
        ProblemSet ps = problemSetService.getById(id);
        if (ps == null) return Result.error(404, "题单不存在");
        return Result.ok(ps);
    }

    /**
     * 获取题单内所有题目详情
     * GET /api/problem-sets/{id}/items
     */
    @GetMapping("/{id}/items")
    public Result<List<ProblemSetItemDetailDTO>> items(@PathVariable Long id) {
        List<ProblemSetItemDetailDTO> items = problemSetService.getProblemSetItems(id);
        return Result.ok(items);
    }

    /**
     * 更新题单基本信息
     * PUT /api/problem-sets/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CreateProblemSetDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        problemSetService.updateProblemSet(id, dto);
        return Result.ok();
    }

    /**
     * 删除题单
     * DELETE /api/problem-sets/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        problemSetService.deleteProblemSet(id, userId);
        return Result.ok();
    }

    /**
     * 向题单添加题目
     * POST /api/problem-sets/{id}/items
     */
    @PostMapping("/{id}/items")
    public Result<Void> addItem(
            @PathVariable Long id,
            @RequestParam String problemSlug,
            @RequestParam(defaultValue = "100") Integer score,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        problemSetService.addProblemToSet(id, problemSlug, score, userId);
        return Result.ok();
    }

    /**
     * 从题单移除题目
     * DELETE /api/problem-sets/{id}/items/{itemId}
     */
    @DeleteMapping("/{id}/items/{itemId}")
    public Result<Void> removeItem(@PathVariable Long id, @PathVariable Long itemId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        problemSetService.removeProblemFromSet(id, itemId, userId);
        return Result.ok();
    }

    /**
     * 调整题单内题目顺序
     * PUT /api/problem-sets/{id}/items/reorder
     */
    @PutMapping("/{id}/items/reorder")
    public Result<Void> reorder(@PathVariable Long id, @RequestBody List<Long> itemIds, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        problemSetService.reorderItems(id, itemIds, userId);
        return Result.ok();
    }
}
