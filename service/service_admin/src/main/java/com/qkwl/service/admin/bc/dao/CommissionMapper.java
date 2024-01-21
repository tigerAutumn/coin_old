package com.qkwl.service.admin.bc.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.qkwl.common.dto.commission.Commission;

@Mapper
public interface CommissionMapper {

	/**
     * 佣金分页查询的总记录数
     * @param map 参数map
     * @return 查询记录数
     */
    int countCommissionListByParam(Map<String, Object> map);
    
    /**
     * 佣金分页查询
     * @param map 参数map
     * @return 用户列表
     */
	List<Commission> getCommissionPageList(Map<String, Object> map);
	
	/**
     * 根据id查询佣金
     * @param 佣金表id
     * @return 佣金记录
     */
	Commission getCommissionById(Integer id);
	
	/**
	 * 修改佣金表
	 * @param commission
	 */
	void updateCommission(Commission commission);
	
	/**
     * 根据邀请人id查询已发放的佣金总和
     * @param map 参数map
     * @return 用户列表
     */
	BigDecimal selectIssuedAmountByIntroId(Integer inviterId);
}
