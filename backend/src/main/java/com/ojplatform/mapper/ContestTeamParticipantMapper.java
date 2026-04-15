package com.ojplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ojplatform.entity.ContestTeamParticipant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 比赛队伍参赛对象数据访问接口。
 */
@Mapper
public interface ContestTeamParticipantMapper extends BaseMapper<ContestTeamParticipant> {
}
