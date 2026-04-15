package com.ojplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ojplatform.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 队伍成员数据访问接口。
 */
@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {
}
