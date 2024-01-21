package com.qkwl.common.rpc.admin;

import java.math.BigDecimal;

import com.qkwl.common.dto.commission.Commission;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.util.ReturnResult;

public interface IAdminCommissionService {

	/**
	 * 分页查询佣金
	 * @param pageParam 分页参数
	 * @param commission 实体参数
	 * @return 分页查询记录列表
	 */
	public Pagination<Commission> selectCommissionPageList(Pagination<Commission> pageParam, Commission commission);
	
	/**
	 * 根据id查询佣金
	 * @param 
	 * @return 佣金记录
	 */
	public Commission selectCommissionById(Integer id);
	
	/**
	 * 发放佣金
	 * @param commission
	 */
	public ReturnResult grantCommission(Commission commission);
	
	/**
	 * 禁止发放
	 * @param commission
	 */
	public ReturnResult forbbinGrant(Commission commission);
	
	/**
	 * 解除禁止
	 * @param commission
	 */
	public ReturnResult relieveForbbin(Commission commission);
	
	/**
	 * 根据邀请人id查询已发放的佣金总和
	 * @param inviterId
	 */
	public BigDecimal selectIssuedAmountByIntroId(Integer inviterId);
	
}
