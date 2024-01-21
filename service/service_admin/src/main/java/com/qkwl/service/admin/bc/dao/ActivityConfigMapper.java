package com.qkwl.service.admin.bc.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.qkwl.common.dto.activity.ActivityConfig;

@Mapper
public interface ActivityConfigMapper {

	/**
     * 活动分页查询的总记录数
     * @param map 参数map
     * @return 查询记录数
     */
    int countActivityListByParam(Map<String, Object> map);
    
    /**
     * 活动分页查询
     * @param map 参数map
     * @return 
     */
	List<ActivityConfig> getActivityPageList(Map<String, Object> map);
	
	/**
     * 根据id查询活动
     * @param id
     * @return
     */
	ActivityConfig getActivityById(Integer id);
	
	/**
     * 新增活动
     * @param activity
     * @return
     */
	void insertActivity(ActivityConfig activity);
	
	/**
     * 删除活动
     * @param id
     * @return 
     */
	void deleteActivity(ActivityConfig activity);
	
	/**
     * 修改活动
     * @param id
     * @return 
     */
	void updateActivity(ActivityConfig activity);
}
