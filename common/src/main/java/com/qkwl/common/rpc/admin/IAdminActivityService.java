package com.qkwl.common.rpc.admin;

import com.qkwl.common.dto.activity.ActivityConfig;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.util.ReturnResult;

public interface IAdminActivityService {

	/**
	 * 分页查询活动
	 * @param pageParam 分页参数
	 * @param status 活动状态
	 * @return 分页查询记录列表
	 */
	public Pagination<ActivityConfig> selectActivityPageList(Pagination<ActivityConfig> pageParam, Integer status);
	
	/**
	 * 根据id查询活动
	 * @param 
	 * @return 活动记录
	 */
	public ActivityConfig selectActivityById(Integer id);
	
	/**
     * 新增活动
     *
     * @param activity 活动
     * @return true：成功，false：失败
     */
	public ReturnResult insertActivity(ActivityConfig activity);
	
	/**
	 * 删除活动
	 * @param commission
	 */
	public ReturnResult deleteActivity(ActivityConfig activity);
	
	/**
	 * 修改活动
	 * @param commission
	 */
	public ReturnResult updateActivity(ActivityConfig activity);
}
