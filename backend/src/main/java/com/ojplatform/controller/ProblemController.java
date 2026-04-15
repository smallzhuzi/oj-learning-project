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
 * 题目相关接口控制器。
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
 * 分页查询题目列表。
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

/**
 * 分页查询题目标签选项。
 */
    @GetMapping("/tags")
    public Result<Page<ProblemTagOptionDTO>> searchTags(
            @RequestParam(defaultValue = "leetcode") String ojPlatform,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        return Result.ok(problemService.searchTagOptions(ojPlatform, keyword, pageNum, pageSize));
    }

/**
 * 查询题目详情。
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
 * 从远程平台同步题目数据。
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
