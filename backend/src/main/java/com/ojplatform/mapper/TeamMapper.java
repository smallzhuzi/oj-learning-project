package com.ojplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ojplatform.entity.Team;
import org.apache.ibatis.annotations.Mapper;

/**
 * 队伍数据访问接口。
 */
@Mapper
public interface TeamMapper extends BaseMapper<Team> {
}
