package com.ojplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ojplatform.dto.SaveDraftDTO;
import com.ojplatform.entity.CodeDraft;

import java.util.List;

public interface CodeDraftService extends IService<CodeDraft> {

    /** 保存/更新草稿（upsert） */
    void saveDraft(SaveDraftDTO dto);

    /** 获取用户某题所有语言的草稿 */
    List<CodeDraft> getDrafts(Long userId, String problemSlug);
}
