package com.coffee.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffee.system.domain.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {
}