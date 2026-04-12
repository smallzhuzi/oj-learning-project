package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ojplatform.dto.SaveDraftDTO;
import com.ojplatform.entity.CodeDraft;
import com.ojplatform.mapper.CodeDraftMapper;
import com.ojplatform.service.CodeDraftService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeDraftServiceImpl extends ServiceImpl<CodeDraftMapper, CodeDraft> implements CodeDraftService {

    @Override
    public void saveDraft(SaveDraftDTO dto) {
        // 按唯一键查询是否已存在
        LambdaQueryWrapper<CodeDraft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeDraft::getUserId, dto.getUserId())
               .eq(CodeDraft::getProblemSlug, dto.getProblemSlug())
               .eq(CodeDraft::getLanguage, dto.getLanguage());
        CodeDraft existing = baseMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setCode(dto.getCode());
            baseMapper.updateById(existing);
        } else {
            CodeDraft draft = new CodeDraft();
            draft.setUserId(dto.getUserId());
            draft.setProblemSlug(dto.getProblemSlug());
            draft.setLanguage(dto.getLanguage());
            draft.setCode(dto.getCode());
            baseMapper.insert(draft);
        }
    }

    @Override
    public List<CodeDraft> getDrafts(Long userId, String problemSlug) {
        LambdaQueryWrapper<CodeDraft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeDraft::getUserId, userId)
               .eq(CodeDraft::getProblemSlug, problemSlug);
        return baseMapper.selectList(wrapper);
    }
}
