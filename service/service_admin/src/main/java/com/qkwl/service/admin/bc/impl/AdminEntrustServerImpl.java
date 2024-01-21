package com.qkwl.service.admin.bc.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.qkwl.common.dto.Enum.EntrustStateEnum;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.entrust.FEntrust;
import com.qkwl.common.dto.entrust.FEntrustHistory;
import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.rpc.admin.IAdminEntrustServer;
import com.qkwl.common.util.DateUtils;
import com.qkwl.common.util.Utils;
import com.qkwl.service.admin.bc.dao.FEntrustHistoryMapper;
import com.qkwl.service.admin.bc.dao.FEntrustMapper;


/**
 * 委单接口实现
 * @author ZKF
 */
@Service("adminEntrustServer")
public class AdminEntrustServerImpl implements IAdminEntrustServer {
	
	private static Logger logger = LoggerFactory.getLogger(AdminEntrustServerImpl.class);
	
	@Autowired
	private FEntrustMapper entrustMapper;
	@Autowired
	private FEntrustHistoryMapper entrustHistoryMapper;
	
	/**
	 * 分页查询委单
	 * @param pageParam 分页参数
	 * @param filterParam 实体参数
	 * @return 分页查询记录列表
	 * @see com.qkwl.common.rpc.admin.IAdminEntrustServer#selectFEntrustList(com.qkwl.common.dto.common.Pagination, com.qkwl.common.dto.entrust.FEntrust)
	 */
	@Override
	public Pagination<FEntrust> selectFEntrustList(Pagination<FEntrust> pageParam, FEntrust filterParam) {
		// 组装查询条件数据
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("offset", pageParam.getOffset());
		map.put("limit", pageParam.getPageSize());
		map.put("orderField", pageParam.getOrderField());
		map.put("orderDirection", pageParam.getOrderDirection());
		map.put("keyword", pageParam.getKeyword());
		map.put("ftype", filterParam.getFtype());
		map.put("ftradeid", filterParam.getFtradeid());
		map.put("fprize", filterParam.getFprize());
		map.put("fstatus", filterParam.getFstatus());
		map.put("fagentid", filterParam.getFagentid());
		
		map.put("start", pageParam.getBegindate());
		map.put("end", pageParam.getEnddate());
		// 查询数据
		List<FEntrust> list = entrustMapper.getAdminPageList(map);
		// 查询总数
		//int count = entrustMapper.countAdminPage(map);
		// 设置返回数据
		pageParam.setData(list);
		if(list.size()<pageParam.getPageSize()){
			pageParam.setTotalRows(pageParam.getOffset());
		}else{
			pageParam.setTotalRows(pageParam.getOffset()+200);
		}
		pageParam.generate();
		return pageParam;
	}

	/**
	 * 撤销委单
	 * @param uId  用户id
	 * @param entrustId 委单id
	 * @return 是否处理成功
	 * @throws BCException 撤单异常
	 * @see com.qkwl.common.rpc.admin.IAdminEntrustServer#updateCancelEntrust(int, java.math.BigInteger)
	 */
	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public boolean updateCancelEntrust(int uId, BigInteger entrustId) throws BCException {
		FEntrust fEntrust = entrustMapper.selectById(uId, entrustId);
		// 状态判断
		if (fEntrust == null) {
			return false;
		}
		if (fEntrust.getFstatus().equals(EntrustStateEnum.Cancel.getCode()) || fEntrust.getFstatus().equals(EntrustStateEnum.AllDeal.getCode()) ||
			fEntrust.getFstatus().equals(EntrustStateEnum.WAITCancel.getCode())) {
			return false;
		}
		// 更改订单状态
		fEntrust.setFstatus(EntrustStateEnum.WAITCancel.getCode());
		fEntrust.setFlastupdattime(Utils.getTimestamp());
		if (this.entrustMapper.updateByfId(fEntrust) <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 查询单个委单记录 
	 * @param entrustId 委单id
	 * @return 委单实体
	 * @see com.qkwl.common.rpc.admin.IAdminEntrustServer#selectFEntrust(int)
	 */
	@Override
	public FEntrust selectFEntrust(int entrustId) {
		return entrustMapper.getById(entrustId);
	}

	/**
	 * 分页查询历史委单
	 * @param pageParam 分页参数
	 * @param filterParam 实体参数
	 * @return 分页查询记录列表
	 * @see com.qkwl.common.rpc.admin.IAdminEntrustServer#selectFEntrustHistoryList(com.qkwl.common.dto.common.Pagination, com.qkwl.common.dto.entrust.FEntrustHistory)
	 */
	@Override
	public Pagination<FEntrustHistory> selectFEntrustHistoryList(Pagination<FEntrustHistory> pageParam, FEntrustHistory filterParam) {
		try {
			// 组装查询条件数据
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("offset", pageParam.getOffset());
			map.put("limit", pageParam.getPageSize());
			map.put("orderField", pageParam.getOrderField());
			map.put("orderDirection", pageParam.getOrderDirection());
			map.put("keyword", pageParam.getKeyword());
			map.put("ftype", filterParam.getFtype());
			map.put("ftradeid", filterParam.getFtradeid());
			map.put("fprize", filterParam.getFprize());
			map.put("fstatus", filterParam.getFstatus());
			map.put("fagentid", filterParam.getFagentid());
			
			map.put("start", pageParam.getBegindate());
			map.put("end", pageParam.getEnddate());

			// 查询总数
//			int count = entrustHistoryMapper.countAdminPage(map);
//			if(count > 0) {
				//按照月份循环，例如当前页是40条，但是8月份查出来的不够40条则需要继续查7月份的，最后塞到list中
//				//获取当前月份
//				int month = 12;
//				List<FEntrustHistory> newList = new ArrayList<FEntrustHistory>();
//				for(int i = 0;i < month;i++){
//					//从当前月开始
//					String startTime = DateUtils.format(DateUtils.getMonthFirstDay(i), "yyyy-MM-dd HH:mm:ss");
//					String endTime = DateUtils.format(DateUtils.getMonthLastDay(i-1), "yyyy-MM-dd HH:mm:ss");
//					map.put("start", startTime);
//					map.put("end", endTime);
					List<FEntrustHistory> list = entrustHistoryMapper.getAdminPageList(map);
//					newList.addAll(list);
//					if(newList.size() >= pageParam.getPageSize()) {
//						break;
//					}
//				}
				// 设置返回数据
				pageParam.setData(list);
				logger.info("=========查询出来的历史委单条数："+list.size());
//			}
			
//			pageParam.setTotalRows(count);
//			if(list.size()<pageParam.getPageSize()){
//				pageParam.setTotalRows(pageParam.getOffset());
//			}else{
//				pageParam.setTotalRows(pageParam.getOffset()+200);
//			}
			pageParam.generate();
			return pageParam;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 查询当天不超过100000的历史委单
	 * @param pageParam 分页参数
	 * @param filterParam 实体参数
	 * @return 分页查询记录列表
	 * @see com.qkwl.common.rpc.admin.IAdminEntrustServer#selectFEntrustHistoryListNoPage(com.qkwl.common.dto.common.Pagination, com.qkwl.common.dto.entrust.FEntrustHistory)
	 */
	@Override
	public List<FEntrustHistory> selectFEntrustHistoryListNoPage(Pagination<FEntrustHistory> pageParam, FEntrustHistory filterParam) {
		// 组装查询条件数据
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("offset", pageParam.getOffset());
		map.put("limit", pageParam.getPageSize());
		map.put("orderField", pageParam.getOrderField());
		map.put("orderDirection", pageParam.getOrderDirection());
		map.put("keyword", pageParam.getKeyword());
		map.put("ftype", filterParam.getFtype());
		map.put("ftradeid", filterParam.getFtradeid());
		map.put("fprize", filterParam.getFprize());
		map.put("fstatus", filterParam.getFstatus());
		
		map.put("start", pageParam.getBegindate());
		map.put("end", pageParam.getEnddate());
		// 查询数据
		List<FEntrustHistory> list = entrustHistoryMapper.getAdminPageList(map);
		// 查询总数
		return list;		
	}

	/**
	 * 根据类型计算委单总量
	 * @param fuid 用户id
	 * @param coinid 币种
	 * @param type 类型
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return 总量统计
	 */
	@Override
	public FEntrust selectCurrentTotalAmountByType(Integer fuid, Integer buycoinid, Integer sellcoinid, Integer type, Integer status, Date start, Date end) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fuid", fuid);
		map.put("buycoinid", buycoinid);
		map.put("sellcoinid", sellcoinid);
		map.put("type", type);
		map.put("status", status);
		map.put("start", start);
		map.put("end", end);
		
		FEntrust entrust = entrustMapper.getTotalAmountByType(map);
		return entrust;		
	}
	
	@Override
	public Map<Integer,FEntrust> selectCurrentTotalAmountByType(Integer fuid, boolean buyCoinIdOrSellCoinId, Integer type, Integer status,
			Date start, Date end,Date date) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("fuid", fuid);
			if(buyCoinIdOrSellCoinId) {
				map.put("buycoinid", 1);
			}else {
				map.put("sellcoinid", 1);
			}
			map.put("status", status);
			map.put("type", type);
			map.put("start", start);
			map.put("end", end);
			map.put("date", date);
			List<FEntrust> totalAmountByTypeList = entrustMapper.getTotalAmountByTypeList(map);
			Map<Integer, FEntrust> collect = null ;
			if(buyCoinIdOrSellCoinId) {
				collect = totalAmountByTypeList.stream().collect(Collectors.toMap(FEntrust::getFbuycoinid, f -> f));
			}else {
				collect = totalAmountByTypeList.stream().collect(Collectors.toMap(FEntrust::getFsellcoinid, f -> f));
			}
			return collect;	
		} catch (Exception e) {
			logger.error("selectTotalAmountByType 异常",e);
			return null;
		}
	}
	
	
	/**
	 * 根据类型计算委单总量
	 * @param fuid 用户id
	 * @param coinid 币种
	 * @param type 类型
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return 总量统计
	 */
	@Override
	public FEntrustHistory selectTotalAmountByType(Integer fuid, Integer buycoinid, Integer sellcoinid, Integer type, Date start, Date end) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fuid", fuid);
		map.put("buycoinid", buycoinid);
		map.put("sellcoinid", sellcoinid);
		map.put("type", type);
		map.put("start", start);
		map.put("end", end);
		
		FEntrustHistory entrust = entrustHistoryMapper.getTotalAmountByType(map);
		return entrust;		
	}

	@Override
	public Map<Integer, FEntrustHistory> selectTotalAmountByType(Integer fuid, boolean buyCoinIdOrSellCoinId, Integer type,
			Date start, Date end,Date date) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("fuid", fuid);
			if(buyCoinIdOrSellCoinId) {
				map.put("buycoinid", 1);
			}else {
				map.put("sellcoinid", 1);
			}
			map.put("type", type);
			map.put("start", start);
			map.put("end", end);
			map.put("date",date);
			List<FEntrustHistory> totalAmountByTypeList = entrustHistoryMapper.getTotalAmountByTypeList(map);
			Map<Integer, FEntrustHistory> collect = null ;
			if(buyCoinIdOrSellCoinId) {
				collect = totalAmountByTypeList.stream().collect(Collectors.toMap(FEntrustHistory::getFbuycoinid, f -> f));
			}else {
				collect = totalAmountByTypeList.stream().collect(Collectors.toMap(FEntrustHistory::getFsellcoinid, f -> f));
			}
			return collect;	
		} catch (Exception e) {
			logger.error("selectTotalAmountByType 异常",e);
			return null;
		}
	}

	@Override
	public FEntrustHistory selectHistoryEntrust(Integer fuid, Integer coinid) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fuid", fuid);
		map.put("coinid", coinid);
		return entrustHistoryMapper.selectHistoryEntrust(map);
	}
	
}
