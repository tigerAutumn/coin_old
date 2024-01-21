package com.qkwl.admin.layui.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.github.pagehelper.PageInfo;
import com.qkwl.admin.layui.base.WebBaseController;
import com.qkwl.common.Excel.XlsExport;
import com.qkwl.common.dto.Enum.c2c.C2CBusinessStatusEnum;
import com.qkwl.common.dto.Enum.c2c.C2CBusinessTypeEnum;
import com.qkwl.common.dto.Enum.c2c.UserC2CEntrustStatusEnum;
import com.qkwl.common.dto.Enum.c2c.UserC2CEntrustTypeEnum;
import com.qkwl.common.dto.admin.FAdmin;
import com.qkwl.common.dto.c2c.C2CBusiness;
import com.qkwl.common.dto.c2c.SystemC2CSetting;
import com.qkwl.common.dto.c2c.UserC2CEntrust;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.system.FSystemBankinfoWithdraw;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.rpc.admin.IAdminC2CService;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.ReturnResult;

@Controller
public class C2CController extends WebBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(C2CController.class);
	
	@Autowired
	private IAdminC2CService adminC2CService;

	@Autowired
	private RedisHelper redisHelper;


	
	//**************************************  商户 ****************************************************** 
	/**
	 * c2c商户管理页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/c2cMerchantList")
	public ModelAndView virtualCapitalOutList(){
		try {
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("c2c/merchantList");
			// 查询
			List<C2CBusiness> selectC2CBusinessList = adminC2CService.selectC2CBusinessList();
			modelAndView.addObject("C2CBusinessList", selectC2CBusinessList);
			return modelAndView;
		} catch (Exception e) {
			logger.error("virtualCapitalOutList 执行异常",e);
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("comm/404");
			return modelAndView;
		}
		
	}

	/**
	 * 商户新增修改页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/goC2CMerchantJSP")
	public ModelAndView goC2CMerchantJSP(
			 @RequestParam(value = "url", required = false) String url,
	         @RequestParam(value = "id", defaultValue = "0") Integer id){
		try {
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName(url);
			Map<Integer,String>  c2CBusinessType = new HashMap<>();
			for (C2CBusinessTypeEnum c2cBusinessTypeEnum : C2CBusinessTypeEnum.values()) {
				c2CBusinessType.put(c2cBusinessTypeEnum.getCode(), c2cBusinessTypeEnum.getValue());
			}
			modelAndView.addObject("C2CBusinessType", c2CBusinessType);
			
			List<SystemCoinType> coinTypeListSystem = redisHelper.getCoinTypeListSystem();
			Map<Integer,String>  coinTypeMap = new HashMap<>();
			for (SystemCoinType systemCoinType : coinTypeListSystem) {
				coinTypeMap.put(systemCoinType.getId(), systemCoinType.getName());
			}
			modelAndView.addObject("coinTypeMap", coinTypeMap);
			// 查询
			if(id != null && id != 0) {
				C2CBusiness selectC2CBusinessById = adminC2CService.selectC2CBusinessById(id);
				if(selectC2CBusinessById != null) {
					modelAndView.addObject("Business", selectC2CBusinessById);
				}
			}
			return modelAndView;
		} catch (Exception e) {
			logger.error("goC2CMerchantListJSP 执行异常{url:"+url +",id:"+id+"}",e);
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("comm/404");
			return modelAndView;
		}
	}
	
	
	/**
	 * 添加商户
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/addMerchant")
	@ResponseBody
	public ReturnResult addMerchant(C2CBusiness c){
		try {
			if(StringUtils.isEmpty(c.getBankAccountName())) {
				return ReturnResult.FAILUER("银行卡用户为空");
			}
			if(StringUtils.isEmpty(c.getBankAddress())) {
				return ReturnResult.FAILUER("开户地址为空");
			}
			if(StringUtils.isEmpty(c.getBankName())) {
				return ReturnResult.FAILUER("银行为空");
			}
			if(StringUtils.isEmpty(c.getBankNumber())) {
				return ReturnResult.FAILUER("银行卡号为空");
			}
			if(StringUtils.isEmpty(c.getBusinessName())) {
				return ReturnResult.FAILUER("商户姓名为空");
			}
			if(StringUtils.isEmpty(c.getOrderTime())) {
				return ReturnResult.FAILUER("成交时间为空");
			}
			if(c.getCoinCount() == null) {
				return ReturnResult.FAILUER("成交金额为空");
			}
			if(c.getOrderCount() == null) {
				return ReturnResult.FAILUER("成交笔数为空");
			}
			if(c.getSortId() == null) {
				return ReturnResult.FAILUER("排序id为空");
			}
			HttpServletRequest request = sessionContextUtils.getContextRequest();
			FAdmin admin = (FAdmin) request.getSession().getAttribute("login_admin");
			int saveC2CBusiness = adminC2CService.saveC2CBusiness(c,admin.getFid());
			if(saveC2CBusiness == 0) {
				return ReturnResult.FAILUER("添加商户失败");
			}
			return ReturnResult.SUCCESS("成功");
		} catch (Exception e) {
			logger.error("goC2CMerchantListJSP 异常，C2CBusiness："+c.toString() , e);
			return ReturnResult.FAILUER("添加商户失败");
		}
	}
	
	/**
	 * 修改商户
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/updateMerchant")
	@ResponseBody
	public ReturnResult updateMerchant(C2CBusiness c){
		try {
			if(StringUtils.isEmpty(c.getBankAccountName())) {
				return ReturnResult.FAILUER("银行卡用户为空");
			}
			if(StringUtils.isEmpty(c.getBankAddress())) {
				return ReturnResult.FAILUER("开户地址为空");
			}
			if(StringUtils.isEmpty(c.getBankName())) {
				return ReturnResult.FAILUER("银行为空");
			}
			if(StringUtils.isEmpty(c.getBankNumber())) {
				return ReturnResult.FAILUER("银行卡号为空");
			}
			if(StringUtils.isEmpty(c.getBusinessName())) {
				return ReturnResult.FAILUER("商户姓名为空");
			}
			if(StringUtils.isEmpty(c.getOrderTime())) {
				return ReturnResult.FAILUER("成交时间为空");
			}
			if(c.getCoinCount() == null) {
				return ReturnResult.FAILUER("成交金额为空");
			}
			if(c.getOrderCount() == null) {
				return ReturnResult.FAILUER("成交笔数为空");
			}
			if(c.getSortId() == null) {
				return ReturnResult.FAILUER("排序id为空");
			}
			if(c.getId() == null) {
				return ReturnResult.FAILUER("id为空");
			}
			HttpServletRequest request = sessionContextUtils.getContextRequest();
			FAdmin admin = (FAdmin) request.getSession().getAttribute("login_admin");
			int saveC2CBusiness = adminC2CService.updateC2CBusinessById(c,admin.getFid());
			if(saveC2CBusiness == 0) {
				return ReturnResult.FAILUER("添加商户失败");
			}
			return ReturnResult.SUCCESS("成功");
		} catch (Exception e) {
			logger.error("goC2CMerchantListJSP 异常，C2CBusiness："+c.toString() , e);
			return ReturnResult.FAILUER("添加商户失败");
		}
	}
	/**
	 * 商户更改状态
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/updateMerchantStatus")
	@ResponseBody
	public ReturnResult updateMerchantStatus(
			@RequestParam(required = false) int status,
			@RequestParam(required = false) int id){
		try {
			C2CBusiness selectC2CBusinessById = adminC2CService.selectC2CBusinessById(id);
			if(selectC2CBusinessById == null) {
				return ReturnResult.FAILUER("商户不存在");
			}
			if(C2CBusinessStatusEnum.Freeze.getCode() != status && C2CBusinessStatusEnum.Normal.getCode() != status) {
				return ReturnResult.FAILUER("状态错误");
			}
			if(selectC2CBusinessById.getStatus() == status) {
				return ReturnResult.SUCCESS("成功");
			}else {
				selectC2CBusinessById.setStatus(status);
				HttpServletRequest request = sessionContextUtils.getContextRequest();
				FAdmin admin = (FAdmin) request.getSession().getAttribute("login_admin");
				int updateC2CBusinessById = adminC2CService.updateC2CBusinessById(selectC2CBusinessById,admin.getFid());
				if(updateC2CBusinessById == 0) {
					return ReturnResult.FAILUER("更新失败");
				}
			}
			return ReturnResult.SUCCESS("成功");
		} catch (Exception e) {
			logger.error("C2CController.updateMerchantStatus 异常:{id:"+id+",status："+status +"}",e);
			return ReturnResult.FAILUER("更新失败");
		}
	}
	
	
	
	//**************************************  委单   ****************************************************** 
	/**
	 * c2c委单页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/c2cEntrustList")
	public ModelAndView c2cEntrustList(
			@RequestParam(value="pageNum", required=false,defaultValue="1") Integer pageNum,
			@RequestParam(required = false ,defaultValue = "0") int status,
			@RequestParam(required = false ,defaultValue = "0") Integer type,
			@RequestParam(required = false ,defaultValue = "0") Integer coinTypeId,
			@RequestParam(required = false ) String businessName,
			@RequestParam(required = false ) String keywords,
			@RequestParam(value = "logDate", required = false) String logDate,
			@RequestParam(value = "endDate", required = false) String endDate){
		try {
			ModelAndView modelAndView = new ModelAndView();
			if(type == UserC2CEntrustTypeEnum.recharge.getCode()) {
				modelAndView.setViewName("c2c/c2cEntrustRechargeList");
			}else if(type == UserC2CEntrustTypeEnum.withdraw.getCode()) {
				modelAndView.setViewName("c2c/c2cEntrustWithdrawList");
			}else {
				modelAndView.setViewName("comm/404");
				return modelAndView;
			}
			modelAndView.addObject("status", status);
			modelAndView.addObject("businessName", businessName);
			modelAndView.addObject("keywords", keywords);
			modelAndView.addObject("coinTypeId", coinTypeId);
			List<SystemCoinType> coinTypeList = redisHelper.getCoinTypeListAll();
			Map<Integer,String> coinTypeMap = new HashMap<>();
			coinTypeMap.put(0, "全部");
			for (SystemCoinType systemCoinType : coinTypeList) {
				coinTypeMap.put(systemCoinType.getId(), systemCoinType.getName());
			}
			modelAndView.addObject("coinTypeMap", coinTypeMap);
			
			Map<Integer,String>  userC2CEntrustStatus = new HashMap<>();
			userC2CEntrustStatus.put(0, "全部");
			for (UserC2CEntrustStatusEnum userEntrustStatus : UserC2CEntrustStatusEnum.values()) {
				userC2CEntrustStatus.put(userEntrustStatus.getCode(), userEntrustStatus.getValue());
			}
			modelAndView.addObject("userC2CEntrustStatus", userC2CEntrustStatus);
			UserC2CEntrust u = new UserC2CEntrust();
			// 开始时间
			if (!StringUtils.isEmpty(logDate)) {
				modelAndView.addObject("logDate", logDate);
				u.setStartTime(logDate);
			}
			// 结束时间
			if (!StringUtils.isEmpty(endDate)) {
				modelAndView.addObject("endDate", endDate);
				u.setEndTime(endDate);
			}
			if(status != 0) {
				u.setStatus(status);
			}
			if(type != 0) {
				u.setType(type);
			}
			if(!StringUtils.isEmpty(businessName)) {
				u.setBusinessName(businessName.trim());
			}
			if(!StringUtils.isEmpty(keywords)) {
				u.setRemark(keywords.trim());
				u.setOrderNumber(keywords.trim());
				if(keywords.matches("[0-9]+")) {
					u.setUserId(Integer.valueOf(keywords));
				}
			}
			if(coinTypeId != 0) {
				u.setCoinId(coinTypeId);
			}
			PageInfo<UserC2CEntrust> selectUserC2CEntrustList = adminC2CService.selectUserC2CEntrustList(u, pageNum, Constant.adminPageSize);
			modelAndView.addObject("UserC2CEntrustList" ,selectUserC2CEntrustList);
			return modelAndView;
		} catch (Exception e) {
			logger.error("virtualCapitalOutList 执行异常",e);
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("comm/404");
			return modelAndView;
		}
		
	}

	
	/**
	 * 查询订单详情
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/c2cEntrustDetails")
	public ModelAndView c2cEntrustDetails(
	         @RequestParam(required = true) Integer entrustId){
		try {
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("c2c/c2cEntrustDetails");
			UserC2CEntrust entrustDetailsById = adminC2CService.getEntrustDetailsById(entrustId);
			modelAndView.addObject("entrustDetails",entrustDetailsById);
			return modelAndView;
		} catch (Exception e) {
			logger.error("c2cEntrustDetails 执行异常{entrustId:"+entrustId +"}",e);
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("comm/404");
			return modelAndView;
		}
	}
	
	// 导出列名
	private static enum ExportFiled {
		币种,订单编号,用户id,用户姓名,提交时间,修改时间,商户昵称,订单备注,订单类型,订单数量,价钱,订单金额,订单来源,审核人,订单状态;
	}
	
	/**
	 * c2c委单页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/entrustOutExport")
	@ResponseBody
	public ReturnResult entrustOutExport(
			//@RequestParam(value="pageNum", required=false,defaultValue="1") Integer pageNum,
			@RequestParam(required = false ,defaultValue = "0") int status,
			@RequestParam(required = false ,defaultValue = "0") Integer type,
			@RequestParam(required = false ) String businessName,
			@RequestParam(required = false ) String keywords,
			@RequestParam(value = "logDate", required = false) String logDate,
			@RequestParam(value = "endDate", required = false) String endDate){
		try {
			HttpServletResponse response = sessionContextUtils.getContextResponse();
			response.setContentType("Application/excel");
			if(type == UserC2CEntrustTypeEnum.recharge.getCode()) {
				response.addHeader("Content-Disposition", "attachment;filename=c2cRechargeEntrust.xls");
			}else if(type == UserC2CEntrustTypeEnum.withdraw.getCode()) {
				response.addHeader("Content-Disposition", "attachment;filename=c2cWithdrawEntrust.xls");
			}
			
			XlsExport e = new XlsExport();
			int rowIndex = 0;
	
			// header
			e.createRow(rowIndex++);
			for (ExportFiled filed : ExportFiled.values()) {
				e.setCell(filed.ordinal(), filed.toString());
			}
			
			UserC2CEntrust u = new UserC2CEntrust();
			// 开始时间
			if (!StringUtils.isEmpty(logDate)) {
				u.setStartTime(logDate);
			}
			// 结束时间
			if (!StringUtils.isEmpty(endDate)) {
				u.setEndTime(endDate);
			}
			if(status != 0) {
				u.setStatus(status);
			}
			if(type != 0) {
				u.setType(type);
			}
			if(!StringUtils.isEmpty(businessName)) {
				u.setBusinessName(businessName.trim());
			}
			if(!StringUtils.isEmpty(keywords)) {
				u.setRemark(keywords.trim());
				u.setOrderNumber(keywords.trim());
				if(keywords.matches("[0-9]+")) {
					u.setUserId(Integer.valueOf(keywords));
				}
			}
			PageInfo<UserC2CEntrust> selectUserC2CEntrustList = adminC2CService.selectUserC2CEntrustList(u, 1, 100000);
			List<UserC2CEntrust> list = selectUserC2CEntrustList.getList();
			for (UserC2CEntrust userC2CEntrust : list) {
				e.createRow(rowIndex++);
				for (ExportFiled filed : ExportFiled.values()) {
					switch (filed) {
					case 币种:
						e.setCell(filed.ordinal(), userC2CEntrust.getCoinName());
						break;
					case 订单编号:
						e.setCell(filed.ordinal(), userC2CEntrust.getOrderNumber());
						break;
					case 用户id:
						e.setCell(filed.ordinal(), userC2CEntrust.getUserId());
						break;
					case 用户姓名:
						e.setCell(filed.ordinal(), userC2CEntrust.getUserName());
						break;
					case 提交时间:
						e.setCell(filed.ordinal(), userC2CEntrust.getCreateTime());
						break;
					case 修改时间:
						e.setCell(filed.ordinal(), userC2CEntrust.getUpdateTime());
						break;
					case 商户昵称:
						e.setCell(filed.ordinal(), userC2CEntrust.getBusinessName());
						break;
					case 订单备注:
						e.setCell(filed.ordinal(), userC2CEntrust.getRemark());
						break;
					case 订单类型:
						e.setCell(filed.ordinal(), userC2CEntrust.getTypeString());
						break;
					case 订单数量:
						e.setCell(filed.ordinal(), userC2CEntrust.getAmount().doubleValue());
						break;
					case 价钱:
						e.setCell(filed.ordinal(), userC2CEntrust.getPrice()==null ? "":String.valueOf(userC2CEntrust.getPrice().doubleValue()));
						break;
					case 订单金额:
						e.setCell(filed.ordinal(), userC2CEntrust.getMoney().doubleValue());
						break;
					case 订单来源:
						e.setCell(filed.ordinal(), userC2CEntrust.getPlatformString());
						break;
					case 审核人:
						e.setCell(filed.ordinal(), userC2CEntrust.getAdminName());
						break;
					case 订单状态:
						e.setCell(filed.ordinal(), userC2CEntrust.getStatusString());
						break;
					default:
						break;
					}
				}
			}
			
			e.exportXls(response);
			return ReturnResult.SUCCESS("导出成功");
		} catch (Exception e) {
			logger.error("c2c委单导出异常",e);
			return ReturnResult.FAILUER("导出失败");
		}
		
	}
		
	/**
	 * 审核通过
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/auditPassThrough")
	@ResponseBody
	public ReturnResult auditPassThrough(
	         @RequestParam(required = true) Integer id,
	         @RequestParam(required = true) Integer type
			){
		try {
			HttpServletRequest request = sessionContextUtils.getContextRequest();
			FAdmin admin = (FAdmin) request.getSession().getAttribute("login_admin");
			return adminC2CService.auditPassThrough(id,admin.getFid(),type);
		} catch (Exception e) {
			logger.error("auditPassThrough 执行异常{entrustId:"+id +"}",e);
			return ReturnResult.FAILUER("审核失败，系统异常");
		}
	}
		
	/**
	 * 锁定
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/lock")
	@ResponseBody
	public ReturnResult lock( @RequestParam(required = true) Integer id){
		try {
			HttpServletRequest request = sessionContextUtils.getContextRequest();
			FAdmin admin = (FAdmin) request.getSession().getAttribute("login_admin");
			return adminC2CService.lock(id,admin.getFid());
		} catch (Exception e) {
			logger.error("lock 执行异常{entrustId:"+id +"}",e);
			return ReturnResult.FAILUER("锁定失败，系统异常");
		}
	}
	
	/**
	 * 驳回申请
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/auditReject")
	@ResponseBody
	public ReturnResult auditReject(
	         @RequestParam(required = true) Integer id,
	         @RequestParam(required = true) Integer type){
		try {
			HttpServletRequest request = sessionContextUtils.getContextRequest();
			FAdmin admin = (FAdmin) request.getSession().getAttribute("login_admin");
			return adminC2CService.auditReject(id,admin.getFid(),type);
		} catch (Exception e) {
			logger.error("auditPassThrough 执行异常{entrustId:"+id +"}",e);
			return ReturnResult.FAILUER("驳回失败，系统异常");
		}
	}
	
	
	
	//**************************************  系统参数  ****************************************************** 
	
	/**
	 * 查询c2c设置参数
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/c2cFunctionSetting")
	public ModelAndView c2cFunctionSetting(){
		try {
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("c2c/c2cFunctionSetting");
			List<SystemC2CSetting> c2cSetting = adminC2CService.getC2CSetting();
			modelAndView.addObject("c2cSettingList",c2cSetting);
			return modelAndView;
		} catch (Exception e) {
			logger.error("c2cFunctionSetting 异常 ",e);
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("comm/404");
			return modelAndView;
		}
	}
	
	
	/**
	 * c2c设置修改页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/goC2CSettingJSP")
	public ModelAndView goC2CSettingJSP(
	         @RequestParam(required = true) Integer id){
		try {
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("c2c/updateC2CSetting");
			SystemC2CSetting c2cSettingById = adminC2CService.getC2CSettingById(id);
			modelAndView.addObject("c2cSetting", c2cSettingById );
			return modelAndView;
		} catch (Exception e) {
			logger.error("goC2CSettingJSP 执行异常{id:"+id+"}",e);
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("comm/404");
			return modelAndView;
		}
	}
	
	/**
	 * 修改c2c设置参数
	 * @return
	 */
	@RequestMapping("admin/c2c/updateC2CFunctionSetting")
	@ResponseBody
	public ReturnResult updateC2CFunctionSetting(
			@RequestParam(required = true) Integer id,
			@RequestParam(required = true) String value
			){
		try {
			HttpServletRequest request = sessionContextUtils.getContextRequest();
			FAdmin admin = (FAdmin) request.getSession().getAttribute("login_admin");
			SystemC2CSetting s = new SystemC2CSetting();
			s.setId(id);
			s.setValue(value);
			int updateC2CSetting = adminC2CService.updateC2CSetting(s, admin.getFid());
			if(updateC2CSetting == 0) {
				return ReturnResult.FAILUER("修改失败");
			}
			return ReturnResult.SUCCESS("成功");
		} catch (Exception e) {
			logger.error("updateC2CFunctionSetting 异常 ",e);
			return ReturnResult.FAILUER("修改失败");
		}
	}
	
	
	//**************************************  银行卡   ****************************************************** 
	
	/**
	 * 银行卡列表
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/goBankInfoJSP")
	public ModelAndView goBankInfoJSP(){
		try {
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("c2c/bankInfoList");
			List<FSystemBankinfoWithdraw> bankInfoList = adminC2CService.getBankInfoList();
			modelAndView.addObject("bankInfoList", bankInfoList );
			return modelAndView;
		} catch (Exception e) {
			logger.error("goBankInfoJSP 执行异常",e);
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("comm/404");
			return modelAndView;
		}
	}
	
	/**
	 * 添加银行页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/addBankInfoJSP")
	public ModelAndView addBankInfoJSP(){
		try {
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("c2c/addBankInfo");
			return modelAndView;
		} catch (Exception e) {
			logger.error("goBankInfoJSP 执行异常",e);
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("comm/404");
			return modelAndView;
		}
	}
	
	/**
	 * 新增银行
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("admin/c2c/addBankInfo")
	public ReturnResult addBankInfo(FSystemBankinfoWithdraw f){
		try {
			if(StringUtils.isEmpty(f.getFcnname())) {
				return ReturnResult.FAILUER("中文名称为空");
			}
			if(StringUtils.isEmpty(f.getFenname())) {
				return ReturnResult.FAILUER("英文名称为空");
			}
			if(StringUtils.isEmpty(f.getFlogo())) {
				return ReturnResult.FAILUER("logo为空");
			}
			if(StringUtils.isEmpty(f.getFtwname())) {
				return ReturnResult.FAILUER("繁文名称为空");
			}
			if(f.getFsort() == null) {
				return ReturnResult.FAILUER("排序id为空");
			}
			f.setFtype(1);
			f.setBankCode("0");
			f.setFstate(true);
			HttpServletRequest request = sessionContextUtils.getContextRequest();
			FAdmin admin = (FAdmin) request.getSession().getAttribute("login_admin");
			int saveC2CBusiness = adminC2CService.saveBankInfo(f, admin.getFid());
			if(saveC2CBusiness == 0) {
				return ReturnResult.FAILUER("添加银行失败");
			}
			return ReturnResult.SUCCESS("成功");
		} catch (Exception e) {
			logger.error("addBankInfo 异常，C2CBusiness："+f.toString() , e);
			return ReturnResult.FAILUER("添加银行失败");
		}
	}
	
	/**
	 * 删除银行
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("admin/c2c/deleteBankInfo")
	@ResponseBody
	public ReturnResult deleteBankInfo(@RequestParam(required = true) Integer fid){
		try {
			HttpServletRequest request = sessionContextUtils.getContextRequest();
			FAdmin admin = (FAdmin) request.getSession().getAttribute("login_admin");
			int deleteBankInfoById = adminC2CService.deleteBankInfoById(fid, admin.getFid());
			if(deleteBankInfoById == 0) {
				return ReturnResult.FAILUER("删除银行失败");
			}
			return ReturnResult.SUCCESS("成功");
		} catch (Exception e) {
			logger.error("deleteBankInfo 执行异常{id:"+fid +"}",e);
			return ReturnResult.FAILUER("删除失败，系统异常");
		}
	}

	
	
}
