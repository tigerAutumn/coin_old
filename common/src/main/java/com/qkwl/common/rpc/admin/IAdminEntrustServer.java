package com.qkwl.common.rpc.admin;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.entrust.FEntrust;
import com.qkwl.common.dto.entrust.FEntrustHistory;

/**
 * 后台委单操作
 * @author ZKF
 */
public interface IAdminEntrustServer {

	/**
	 * 分页查询委单
	 * @param pageParam 分页参数
	 * @param filterParam 实体参数
	 * @return 分页查询记录列表
	 */
	public Pagination<FEntrust> selectFEntrustList(Pagination<FEntrust> pageParam, FEntrust filterParam);
	
	/**
	 * 分页查询历史委单
	 * @param pageParam 分页参数
	 * @param filterParam 实体参数
	 * @return 分页查询记录列表
	 */
	public Pagination<FEntrustHistory> selectFEntrustHistoryList(Pagination<FEntrustHistory> pageParam, FEntrustHistory filterParam);
	
	
	/**
	 * 查询当天不超过100000的历史委单
	 * @param pageParam 分页参数
	 * @param filterParam 实体参数
	 * @return 分页查询记录列表
	 */
	public List<FEntrustHistory> selectFEntrustHistoryListNoPage(Pagination<FEntrustHistory> pageParam, FEntrustHistory filterParam);
	
	/**
	 * 查询单个委单记录 
	 * @param entrustId 委单id
	 * @return 委单实体
	 */
	public FEntrust selectFEntrust(int entrustId);
	
	/**
	 * 撤销委单
	 * @param uId  用户id
	 * @param entrustId 委单id
	 * @return 是否处理成功
	 * @throws BCException 撤单异常
	 */
	public boolean updateCancelEntrust(int uId, BigInteger entrustId) throws BCException;
	
	/**
	 * 根据类型计算委单总量
	 * @param fuid 用户id
	 * @param coinid 币种
	 * @param type 类型
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return 总量统计
	 */
	FEntrust selectCurrentTotalAmountByType(Integer fuid, Integer buycoinid, Integer sellcoinid, Integer type, Integer status, Date start, Date end);
	
	/**
	 * 根据类型计算委单总量
	 * @param fuid 用户id
	 * @param buyCoinIdOrSellCoinId true为买币，false卖币
	 * @param type 类型
	 * @param start 开始时间
	 * @param end 结束时间
	 * @param date 截止时间
	 * @return 总量统计
	 */
	Map<Integer,FEntrust> selectCurrentTotalAmountByType(Integer fuid, boolean buyCoinIdOrSellCoinId, Integer type, Integer status, Date start, Date end,Date date);
	
	
	/**
	 * 根据类型计算委单总量
	 * @param fuid 用户id
	 * @param coinid 币种
	 * @param type 类型
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return 总量统计
	 */
	FEntrustHistory selectTotalAmountByType(Integer fuid, Integer buycoinid, Integer sellcoinid, Integer type, Date start, Date end);
	
	/**
	 * 根据类型计算委单总量
	 * @param fuid 用户id
	 * @param buyCoinIdOrSellCoinId true为买币，false卖币
	 * @param type 类型
	 * @param start 开始时间
	 * @param end 结束时间
	 * @param data 截止时间
	 * @return 总量统计
	 */
	Map<Integer,FEntrustHistory> selectTotalAmountByType(Integer fuid, boolean buyCoinIdOrSellCoinId, Integer type, Date start, Date end,Date data);
	
	/**
	 * 根据uid查询历史委单
	 * @param uid
	 * @return
	 */
	FEntrustHistory selectHistoryEntrust(Integer uid, Integer coinid);
}
