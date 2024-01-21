package com.qkwl.common.rpc.admin;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.capital.FRewardCodeDTO;

/**
 * 兑换码接口
 * @author ZKF
 */
public interface IAdminRewardCodeService {
	
	/**
	 * 分页查询兑换码
	 * @param page 分页参数
	 * @param rc 实体参数
	 * @return 分页查询记录列表
	 */
	Pagination<FRewardCodeDTO> selectRewardCodePageList(Pagination<FRewardCodeDTO> page, FRewardCodeDTO rc);
	
	
	/**
	 * 新增兑换码
	 * @param rc 兑换码实体
	 * @return 是否新增成功
	 */
	public boolean insertRewardCode(FRewardCodeDTO rc);
	
	
	/**
	 * 批量新增兑换码
	 * @param list 兑换码列表
	 * @return 是否执行成功
	 */
	public boolean insertRewardCodeList(List<FRewardCodeDTO> list);
	
	/**
	 * 删除兑换码
	 * @param id 兑换码id
 	 * @return 是否删除成功
	 */
	public boolean deleteRewardCode(int id);

	/**
	 * 查询用户兑换总量
	 * @param fuid 用户id
	 * @param coinid 币种
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return 总量
	 */
	BigDecimal selectWalletTotalAmount(Integer fuid, Integer coinid, Date rwbegindate, Date rwenddate);
	
	/**
	 * 查询用户兑换总量
	 * @param fuid 用户id
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return Map<币种id，总量>
	 */
	Map<Integer,BigDecimal> selectWalletTotalAmount(Integer fuid, Date rwbegindate, Date rwenddate);

}
