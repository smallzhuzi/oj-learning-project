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
 * 代码草稿相关接口控制器。
 */
@RestController
@RequestMapping("/api/drafts")
public class CodeDraftController {

    @Autowired
    private CodeDraftService codeDraftService;

/**
 * 保存当前代码草稿。
 */
    @PutMapping
    public Result<Void> save(@Valid @RequestBody SaveDraftDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        codeDraftService.saveDraft(dto);
        return Result.ok();
    }

/**
 * 查询当前用户的代码草稿列表。
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
