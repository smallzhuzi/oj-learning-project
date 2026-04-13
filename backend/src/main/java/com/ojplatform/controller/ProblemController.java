package com.ojplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ojplatform.common.Result;
import com.ojplatform.dto.ProblemDTO;
import com.ojplatform.dto.ProblemQueryDTO;
import com.ojplatform.dto.ProblemTagOptionDTO;
import com.ojplatform.entity.Problem;
import com.ojplatform.service.OjApiService;
import com.ojplatform.service.OjApiServiceFactory;
import com.ojplatform.service.ProblemTagFacadeService;
import com.ojplatform.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目控制器
 * 提供题目列表查询、题目详情获取、远程同步等接口
 */
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @Autowired
    private OjApiServiceFactory ojApiServiceFactory;

    @Autowired
    private ProblemTagFacadeService problemTagFacadeService;

    /**
     * 分页查询题目列表
     * 支持按关键词搜索（题号/标题）和难度筛选
     *
     * GET /api/problems?keyword=两数&difficulty=Easy&pageNum=1&pageSize=20
     */
    @GetMapping
    public Result<IPage<ProblemDTO>> list(ProblemQueryDTO queryDTO) {
        IPage<Problem> page = problemService.queryProblems(queryDTO);

        Page<ProblemDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setPages(page.getPages());
        dtoPage.setRecords(page.getRecords().stream()
                .map(problem -> ProblemDTO.fromProblem(problem, problemTagFacadeService.getUnifiedTags(problem)))
                .toList());
        return Result.ok(dtoPage);
    }

    @GetMapping("/tags")
    public Result<Page<ProblemTagOptionDTO>> searchTags(
            @RequestParam(defaultValue = "leetcode") String ojPlatform,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        return Result.ok(problemService.searchTagOptions(ojPlatform, keyword, pageNum, pageSize));
    }

    /**
     * 根据 slug 获取题目详情
     *
     * GET /api/problems/{slug}?ojPlatform=leetcode
     */
    @GetMapping("/{slug}")
    public Result<ProblemDTO> detail(
            @PathVariable String slug,
            @RequestParam(defaultValue = "leetcode") String ojPlatform) {
        Problem problem = problemService.getBySlug(slug, ojPlatform);
        if (problem == null) {
            return Result.error(404, "题目不存在");
        }
        return Result.ok(ProblemDTO.fromProblem(problem, problemTagFacadeService.getUnifiedTags(problem)));
    }

    /**
     * 从远程 OJ 同步题目列表到本地数据库
     * 用于首次部署或题库更新时批量拉取题目基础信息
     *
     * POST /api/problems/sync?skip=0&limit=50&ojPlatform=leetcode
     */
    @PostMapping("/sync")
    public Result<Integer> syncFromRemote(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "leetcode") String ojPlatform) {
        OjApiService apiService = ojApiServiceFactory.getService(ojPlatform);
        List<Problem> remoteProblems = apiService.fetchProblemList(skip, limit, keyword);
        int savedCount = 0;
        for (Problem remote : remoteProblems) {
            // 按 slug + platform 去重，本地已存在则跳过
            Problem existing = problemService.getBySlug(remote.getSlug(), ojPlatform);
            if (existing == null) {
                remote.setOjPlatform(ojPlatform);
                problemService.save(remote);
                savedCount++;
            }
        }
        return Result.ok(savedCount);
    }
}
