package com.ojplatform.controller;

import com.ojplatform.common.Result;
import com.ojplatform.dto.SaveDraftDTO;
import com.ojplatform.entity.CodeDraft;
import com.ojplatform.service.CodeDraftService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代码草稿控制器
 * 支持自动保存和手动恢复用户的代码草稿
 */
@RestController
@RequestMapping("/api/drafts")
public class CodeDraftController {

    @Autowired
    private CodeDraftService codeDraftService;

    /**
     * 保存草稿（upsert）
     * PUT /api/drafts
     */
    @PutMapping
    public Result<Void> save(@Valid @RequestBody SaveDraftDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        codeDraftService.saveDraft(dto);
        return Result.ok();
    }

    /**
     * 获取某题所有语言的草稿
     * GET /api/drafts/{problemSlug}
     */
    @GetMapping("/{problemSlug}")
    public Result<List<CodeDraft>> getAll(
            @PathVariable String problemSlug,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<CodeDraft> drafts = codeDraftService.getDrafts(userId, problemSlug);
        return Result.ok(drafts);
    }
}
