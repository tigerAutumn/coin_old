package com.qkwl.admin.layui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.admin.layui.base.WebBaseController;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.redis.MemCache;
import com.qkwl.common.redis.RedisConstant;
import com.qkwl.common.redis.RedisObject;
import com.qkwl.common.util.ArgsConstant;
import com.qkwl.common.util.ReturnResult;

@Controller
public class PromptController extends WebBaseController {

	@Autowired
    private RedisHelper redisHelper;
	@Autowired
	private MemCache memCache;
	
	/**
	 * 升级判断接口
	 */
	@RequestMapping(value = "/prompt/getPrompt")
	@ResponseBody
    public ReturnResult getPrompt() {
		JSONObject jsonObject = new JSONObject();
		String updatingPrompt = redisHelper.getSystemArgs(ArgsConstant.UPDATING_PROMPT);
		if (updatingPrompt != null) {
			String[] split = updatingPrompt.split(",");
			String isUpdating = split[0];
			jsonObject.put("isUpdating", isUpdating);
			String startTime = split[1];
			jsonObject.put("startTime", startTime);
			String endTime = split[2];
			jsonObject.put("endTime", endTime);
		} else {
			jsonObject.put("isUpdating", "0");
		}
		return ReturnResult.SUCCESS(jsonObject);
    }
	
	
	/**
	 * 维护状态页面
	 */
	@RequestMapping(value = "/prompt/promptList")
	public ModelAndView getUpdatingPrompt() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("prompt/promptList");
		String updatingPrompt = redisHelper.getSystemArgs(ArgsConstant.UPDATING_PROMPT);
		if (updatingPrompt != null) {
			String[] split = updatingPrompt.split(",");
			if ("0".equals(split[0])) {
				modelAndView.addObject("isUpdating", "否");
			} else {
				modelAndView.addObject("isUpdating", "是");
			}
			String startTime = split[1];
			modelAndView.addObject("startTime", startTime);
			String endTime = split[2];
			modelAndView.addObject("endTime", endTime);
		}
		return modelAndView;
	}
	
	
	/**
	 * 修改维护状态页面
	 */
	@RequestMapping(value = "/prompt/updatePrompt")
	public ModelAndView updatePrompt() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("prompt/updatePrompt");
		String updatingPrompt = redisHelper.getSystemArgs(ArgsConstant.UPDATING_PROMPT);
		if (updatingPrompt != null) {
			String[] split = updatingPrompt.split(",");
			String isUpdating = split[0];
			modelAndView.addObject("isUpdating", isUpdating);
			String startTime = split[1];
			modelAndView.addObject("startTime", startTime);
			String endTime = split[2];
			modelAndView.addObject("endTime", endTime);
		}
		return modelAndView;
	}
	
	/**
	 * 保存修改
	 */
	@RequestMapping("prompt/savePrompt")
    @ResponseBody
    public ReturnResult savePrompt(
            @RequestParam(value = "isUpdating", required = false) String isUpdating,
            @RequestParam(value = "startTime", required = true) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime
            ) throws Exception {

        String updatingPrompt = isUpdating + "," + startTime + "," + endTime;
        RedisObject args = new RedisObject();
		args.setExtObject(updatingPrompt);
		memCache.setNoExpire(RedisConstant.ARGS_KET + "updatingPrompt", JSON.toJSONString(args));
        return ReturnResult.SUCCESS("更新成功");
    }
}
