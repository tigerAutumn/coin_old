package com.qkwl.admin.layui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qkwl.admin.layui.base.WebBaseController;
import com.qkwl.common.dto.commission.Commission;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.rpc.admin.IAdminCommissionService;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.ReturnResult;

@Controller
public class CommissionController extends WebBaseController {

	@Autowired
	private IAdminCommissionService adminCommissionService;
	
	/**
	 * 分页大小
	 */	
 	private int numPerPage = Constant.adminPageSize;
	
 	/**
 	 * 查询佣金信息
 	 */
	@RequestMapping("/admin/commissionList")
	public ModelAndView getCommissionList(
			@RequestParam(value="inviterId",required=false,defaultValue="0") Integer inviterId,
			@RequestParam(value="inviterLoginname",required=false) String inviterLoginname,
			@RequestParam(value="inviteeId",required=false,defaultValue="0") Integer inviteeId,
			@RequestParam(value="inviteeLoginname",required=false) String inviteeLoginname,
			@RequestParam(value="startDate",required=false) String startDate,
			@RequestParam(value="endDate",required=false) String endDate,
			@RequestParam(value="status",required=false,defaultValue="0") Integer status,
			@RequestParam(value="pageNum",required=false,defaultValue="1") Integer currentPage,
			@RequestParam(value="orderField",required=false,defaultValue="merchandise_time") String orderField,
			@RequestParam(value="orderDirection",required=false,defaultValue="desc") String orderDirection
			) throws Exception {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("commission/commissionList");
		// 定义查询条件
		Pagination<Commission> pageParam = new Pagination<Commission>(currentPage, numPerPage);
		Commission commission = new Commission();
		//参数判断
		if (inviterId > 0){
			commission.setInviterId(inviterId);
			modelAndView.addObject("inviterId", inviterId);
		}
		if (!StringUtils.isEmpty(inviterLoginname)){
			commission.setInviterLoginname(inviterLoginname);
			modelAndView.addObject("inviterLoginname", inviterLoginname);
		}
		if (inviteeId > 0){
			commission.setInviterId(inviteeId);
			modelAndView.addObject("inviteeId", inviteeId);
		}
		if (!StringUtils.isEmpty(inviteeLoginname)){
			commission.setInviteeLoginname(inviteeLoginname);
			modelAndView.addObject("inviteeLoginname", inviteeLoginname);
		}
		if (!StringUtils.isEmpty(startDate)){
			pageParam.setBegindate(startDate);
			modelAndView.addObject("startDate", startDate);
		}
		if (!StringUtils.isEmpty(endDate)){
			pageParam.setEnddate(startDate);
			modelAndView.addObject("endDate", endDate);
		}
		if (status > 0) {
			commission.setStatus(status);
			modelAndView.addObject("status", status);
		}
		
		pageParam.setOrderDirection(orderDirection);
		pageParam.setOrderField(orderField);

		if(inviterId == 0 && StringUtils.isEmpty(inviterLoginname) && inviteeId == 0 && StringUtils.isEmpty(inviteeLoginname) 
				&& StringUtils.isEmpty(startDate) && StringUtils.isEmpty(endDate) && status == 0){
			return modelAndView;
		}
		//查询佣金列表
		Pagination<Commission> pagination = adminCommissionService.selectCommissionPageList(pageParam, commission);

		modelAndView.addObject("commissionList", pagination);
		return modelAndView;
	}
	
	/**
	 * 发放佣金
	 * @param id
	 * @return
	 */
	@RequestMapping("commission/grantCommission")
	@ResponseBody
	public ReturnResult grantCommission(
			@RequestParam(value = "id", required = false,defaultValue="0") Integer id) {
		Commission commission = new Commission();
		if (id > 0) {
			commission = adminCommissionService.selectCommissionById(id);
		}
		if (commission == null) {
			return ReturnResult.FAILUER("佣金记录不存在!");
		}
		if (commission.getStatus() == 2) {
			return ReturnResult.FAILUER("佣金已发放过!");
		}
		if (commission.getStatus() == 3) {
			return ReturnResult.FAILUER("佣金禁止发放!");
		}
		//发放佣金
		return adminCommissionService.grantCommission(commission);
	}
	
	/**
	 * 禁止发放
	 * @param id
	 * @return
	 */
	@RequestMapping("commission/forbbinGrant")
	@ResponseBody
	public ReturnResult forbbinGrant(
			@RequestParam(value = "id", required = false,defaultValue="0") Integer id) {
		Commission commission = new Commission();
		if (id > 0) {
			commission = adminCommissionService.selectCommissionById(id);
		}
		if (commission == null) {
			return ReturnResult.FAILUER("佣金记录不存在!");
		}
		if (commission.getStatus() == 2) {
			return ReturnResult.FAILUER("佣金已发放过!");
		}
		if (commission.getStatus() == 3) {
			return ReturnResult.FAILUER("佣金发放已禁止过!");
		}
		//发放佣金
		return adminCommissionService.forbbinGrant(commission);
	}
	
	/**
	 * 解除禁止
	 * @param id
	 * @return
	 */
	@RequestMapping("commission/relieveForbbin")
	@ResponseBody
	public ReturnResult relieveForbbin(
			@RequestParam(value = "id", required = false,defaultValue="0") Integer id) {
		Commission commission = new Commission();
		if (id > 0) {
			commission = adminCommissionService.selectCommissionById(id);
		}
		if (commission == null) {
			return ReturnResult.FAILUER("佣金记录不存在!");
		}
		if (commission.getStatus() != 3) {
			return ReturnResult.FAILUER("佣金发放未被禁止!");
		}
		
		//发放佣金
		return adminCommissionService.relieveForbbin(commission);
	}
}


