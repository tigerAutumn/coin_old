package com.qkwl.common.rpc.commission;

import java.math.BigDecimal;
import java.util.List;

import com.qkwl.common.dto.activity.ActivityConfig;
import com.qkwl.common.dto.commission.Commission;
import com.qkwl.common.dto.common.Pagination;

public interface ICommissionService {

	/**
	 * 根据邀请人id查询佣金记录
	 * @param inviterId
	 */
	public List<Commission> selectCommissionByIntroId(Integer inviterId);
	
	/**
	 * 根据邀请人id查询佣金总和
	 * @param inviterId
	 */
	public BigDecimal selectAmountByIntroId(Integer inviterId);
	
	/**
	 * 分页查询佣金
	 * @param pageParam 分页参数
	 * @param commission 实体参数
	 * @return 分页查询记录列表
	 */
	public Pagination<Commission> selectCommissionPageList(Pagination<Commission> pageParam, Commission commission);
	
	/**
     * 根据活动id查询活动记录
     * @param id
     * @return 
     */
	public ActivityConfig selectActivityById(Integer id);
}
