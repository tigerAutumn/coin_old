package com.qkwl.service.commission.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.qkwl.common.dto.commission.Commission;
import com.qkwl.common.dto.commission.CommissionRankList;

@Mapper
public interface CommissionMapper {

	/**
     * 根据邀请人id查询佣金记录
     * @param inviterId
     * @return 
     */
	List<Commission> selectCommissionByIntroId(Integer inviterId);
	
	/**
     * 根据邀请人id查询佣金总和
     * @param map 参数map
     * @return 用户列表
     */
	BigDecimal selectAmountByIntroId(Integer inviterId);
	
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
     * 统计首页榜单
     * @param 
     * @return 首页榜单
     */
	List<CommissionRankList> selectIndexRankList(Map<String, Object> map);
	
	/**
     * 统计榜单各页数据
     * @param
     * @return
     */
	List<CommissionRankList> selectRankList(Map<String, Object> map);
	
	/**
     * 统计榜单总条数
     * @param map 参数map
     * @return 用户列表
     */
	List<CommissionRankList> selectRankCount(Map<String, Object> map);
	
	/**
	 * 插入佣金
	 * @param commission
	 * @return
	 */
	void addCommission(Commission commission);
}
