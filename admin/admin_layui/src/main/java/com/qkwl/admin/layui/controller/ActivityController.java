package com.qkwl.admin.layui.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qkwl.common.dto.activity.ActivityConfig;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.rpc.admin.IAdminActivityService;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.ReturnResult;

@Controller
public class ActivityController {

	@Autowired
	private IAdminActivityService adminActivityService;
	
	/**
	 * 分页大小
	 */	
 	private int numPerPage = Constant.adminPageSize;
	
 	/**
 	 * 查询活动信息
 	 */
	@RequestMapping("/admin/activityList")
	public ModelAndView getActivityList(
			@RequestParam(value="status",required=false,defaultValue="0") Integer status,
			@RequestParam(value="pageNum",required=false,defaultValue="1") Integer currentPage,
			@RequestParam(value="orderField",required=false,defaultValue="start_time") String orderField,
			@RequestParam(value="orderDirection",required=false,defaultValue="desc") String orderDirection
			) throws Exception {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("activity/activityList");
		// 定义查询条件
		Pagination<ActivityConfig> pageParam = new Pagination<ActivityConfig>(currentPage, numPerPage);
		//参数判断
		if (status > 0) {
			modelAndView.addObject("status", status);
		}
		
		pageParam.setOrderDirection(orderDirection);
		pageParam.setOrderField(orderField);

		//查询活动列表
		Pagination<ActivityConfig> pagination = adminActivityService.selectActivityPageList(pageParam, status);

		modelAndView.addObject("activityList", pagination);
		return modelAndView;
	}
	
	/**
	 * 删除活动
	 * @param id
	 * @return
	 */
	@RequestMapping("/admin/deleteActivity")
	@ResponseBody
	public ReturnResult deleteActivity(
			@RequestParam(value = "id", required = false,defaultValue="0") Integer id) {
		ActivityConfig activity = new ActivityConfig();
		if (id > 0) {
			activity = adminActivityService.selectActivityById(id);
		}
		if (activity == null) {
			return ReturnResult.FAILUER("活动不存在!");
		}
		//删除活动
		return adminActivityService.deleteActivity(activity);
	}
	
	/**
	 * 添加活动
	 * @return
	 */
	@RequestMapping("/admin/addActivity")
	public ModelAndView addActivity(){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("activity/addActivity");
		return modelAndView;
	}
	
	/**
     * 保存新增的币种信息
     */
    @RequestMapping("admin/saveActivity")
    @ResponseBody
    public ReturnResult saveActivity(
            @RequestParam(value = "activityName", required = false) String activityName,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) throws Exception {
    	ActivityConfig activity = new ActivityConfig();
        activity.setActivityName(activityName);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        activity.setStartTime(format.parse(startTime));
        activity.setEndTime(format.parse(endTime));
        return adminActivityService.insertActivity(activity);
    }
	
	/**
	 * 读取修改的活动信息
	 * @return
	 */
	@RequestMapping("/admin/changeActivity")
	public ModelAndView changeActivity(@RequestParam(value="id",required=false) Integer id){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("/activity/changeActivity");
		if (id > 0) {
			ActivityConfig activity = adminActivityService.selectActivityById(id);
			modelAndView.addObject("activity", activity);
		}
		return modelAndView;
	}
	
	/**
     * 修改的币种信息
     */
    @RequestMapping("admin/updateActivity")
    @ResponseBody
    public ReturnResult updateActivity(
    		@RequestParam(value = "id", required = true,defaultValue = "0") Integer id,
    		@RequestParam(value = "activityName", required = false) String activityName,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) throws Exception {
    	ActivityConfig activity = new ActivityConfig();
		if (id > 0) {
			activity = adminActivityService.selectActivityById(id);
		}
		if (activity == null) {
			return ReturnResult.FAILUER("活动不存在!");
		}
		activity.setActivityName(activityName);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        activity.setStartTime(format.parse(startTime));
        activity.setEndTime(format.parse(endTime));
		
        return adminActivityService.updateActivity(activity);
    }
}
