package com.qkwl.service.admin.bc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.qkwl.common.dto.c2c.SystemC2CSetting;

@Mapper
public interface SystemC2CSettingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SystemC2CSetting record);

    int insertSelective(SystemC2CSetting record);

    SystemC2CSetting selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SystemC2CSetting record);

    int updateByPrimaryKey(SystemC2CSetting record);

	List<SystemC2CSetting> selectAll();
}