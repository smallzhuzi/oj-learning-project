package com.ojplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ojplatform.entity.TeamJoinRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 队伍Join请求数据访问接口。
 */
@Mapper
public interface TeamJoinRequestMapper extends BaseMapper<TeamJoinRequest> {
}
