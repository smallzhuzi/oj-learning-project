package com.ojplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ojplatform.entity.UserOjConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户判题配置数据访问接口。
 */
@Mapper
public interface UserOjConfigMapper extends BaseMapper<UserOjConfig> {
}
