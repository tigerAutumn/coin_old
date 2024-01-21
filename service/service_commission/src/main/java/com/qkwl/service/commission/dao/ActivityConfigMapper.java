package com.qkwl.service.commission.dao;

import org.apache.ibatis.annotations.Mapper;

import com.qkwl.common.dto.activity.ActivityConfig;

@Mapper
public interface ActivityConfigMapper {

	/**
     * 根据活动id查询活动记录
     * @param id
     * @return 
     */
	ActivityConfig selectActivityById(Integer id);
}
