package com.qkwl.service.admin.bc.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qkwl.common.dto.activity.ActivityConfig;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.rpc.admin.IAdminActivityService;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.service.admin.bc.dao.ActivityConfigMapper;

@Service("adminActivityService")
public class AdminActivityServiceImpl implements IAdminActivityService {

	private static final Logger logger = LoggerFactory.getLogger(AdminActivityServiceImpl.class);
	
	@Autowired
	private ActivityConfigMapper activityConfigMapper;
	
	@Override
	public Pagination<ActivityConfig> selectActivityPageList(Pagination<ActivityConfig> pageParam, Integer status) {
		// 组装查询条件数据
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("offset", pageParam.getOffset());
		map.put("limit", pageParam.getPageSize());
		map.put("orderField", pageParam.getOrderField());
		map.put("orderDirection", pageParam.getOrderDirection());
		
		map.put("status", status);
		// 查询总返佣数
		int count = activityConfigMapper.countActivityListByParam(map);
		if(count > 0) {
			// 查询返佣列表
			List<ActivityConfig> activityList = activityConfigMapper.getActivityPageList(map);
			// 设置返回数据
			pageParam.setData(activityList);
		}
		pageParam.setTotalRows(count);
		return pageParam;
	}
	
	@Override
	public ActivityConfig selectActivityById(Integer id) {
		return activityConfigMapper.getActivityById(id);
	}
	
	@Override
	public ReturnResult deleteActivity(ActivityConfig activity) {
		try {
			//删除活动
			activityConfigMapper.deleteActivity(activity);
			return ReturnResult.SUCCESS("删除活动成功！");
		} catch (Exception e) {
			logger.error("删除活动异常，id："+activity.getId(),e);
			return ReturnResult.FAILUER("删除活动失败！");
		}
	}
	
	@Override
	public ReturnResult insertActivity(ActivityConfig activity) {
		try {
			//删除活动
			activityConfigMapper.insertActivity(activity);
			return ReturnResult.SUCCESS("新增活动成功！");
		} catch (Exception e) {
			logger.error("新增活动异常，id："+activity.getId(),e);
			return ReturnResult.FAILUER("新增活动失败！");
		}
	}
	
	@Override
	public ReturnResult updateActivity(ActivityConfig activity) {
		try {
			//删除活动
			activityConfigMapper.updateActivity(activity);
			return ReturnResult.SUCCESS("修改活动成功！");
		} catch (Exception e) {
			logger.error("删除活动异常，id："+activity.getId(),e);
			return ReturnResult.FAILUER("修改活动失败！");
		}
	}
}
