package com.qkwl.common.rpc.admin;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import com.qkwl.common.dto.c2c.C2CBusiness;
import com.qkwl.common.dto.c2c.SystemC2CSetting;
import com.qkwl.common.dto.c2c.UserC2CEntrust;
import com.qkwl.common.dto.system.FSystemBankinfoWithdraw;
import com.qkwl.common.util.ReturnResult;

public interface IAdminC2CService {

	/**
	 * 获取c2c商户列表
	 */
	List<C2CBusiness>  selectC2CBusinessList();
	
	/**
	 * 根据主键ID查询实体
	 * @param id 主键ID
	 * @return
	 */
	C2CBusiness selectC2CBusinessById(Integer id);
	
	/**
	 * 根据主键ID更新
	 * @param c2CBusiness 
	 * @return
	 */
	int updateC2CBusinessById(C2CBusiness c2CBusiness,Integer adminId);
	
	/**
	 * 添加
	 * @param c2CBusiness 
	 * @return
	 */
	int saveC2CBusiness(C2CBusiness c2CBusiness,Integer adminId);

	
	/**
	 * 查询委单
	 * @param UserC2CEntrust 
	 * @return
	 */
	PageInfo<UserC2CEntrust> selectUserC2CEntrustList(UserC2CEntrust u,Integer pageNum,Integer pageSize);

	/**
	 * 通过id查询委单
	 * @param UserC2CEntrust 
	 * @return
	 */
	UserC2CEntrust getEntrustDetailsById(Integer entrustId);

	
	/**
	 * 审核通过
	 * @param id 
	 * @return
	 */
	ReturnResult auditPassThrough(Integer id,Integer adminId,Integer type);
	
	/**
	 * 审核驳回
	 * @param id 
	 * @return
	 */
	ReturnResult auditReject(Integer id,Integer adminId,Integer type);
	
	/**
	 * 锁定
	 * @param id 
	 * @return
	 */
	ReturnResult lock(Integer id,Integer adminId);
	
	
	/**
	 * 查询c2c设置
	 * @return
	 */
	List<SystemC2CSetting> getC2CSetting();
	
	/**
	 * 通过Id查询c2c设置
	 * @return
	 */
	SystemC2CSetting getC2CSettingById(Integer id);
	
	/**
	 * 通过id修改设置
	 * @return
	 */
	int updateC2CSetting(SystemC2CSetting systemC2CSetting,Integer adminId);

	/**
	 * 根据userId,时间统计充提数量
	 * @return
	 */
	Map<Integer,UserC2CEntrust> statisticsRechargeWithdrawTotal(Integer userId,Date date);
	
	/**
	 * 查询所有银行
	 * @return
	 */
	List<FSystemBankinfoWithdraw> getBankInfoList();
	
	/**
	 * 新增银行
	 * @return
	 */
	int saveBankInfo(FSystemBankinfoWithdraw fSystemBankinfoWithdraw,Integer adminId);
	
	/**
	 * 删除银行
	 * @return
	 */
	int deleteBankInfoById(Integer id,Integer adminId);

}
