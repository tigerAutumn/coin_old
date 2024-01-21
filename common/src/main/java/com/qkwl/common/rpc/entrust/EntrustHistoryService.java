package com.qkwl.common.rpc.entrust;

import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.entrust.FEntrustHistory;
import com.qkwl.common.dto.entrust.FEntrustLog;

import java.math.BigInteger;
import java.util.List;

/**
 * 委单接口
 *
 * @author TT
 */
public interface EntrustHistoryService {

    /**
     * 分页查询历史当前委单
     *
     * @param paginParam 分页实体对象
     * @param entrust    历史委单实体
     * @param stateList  委单状态列表
     * @return 分页实体对象
     */
    Pagination<FEntrustHistory> listEntrustHistory(Pagination<FEntrustHistory> paginParam, FEntrustHistory entrust, List<Integer> stateList);

    /**
     * 根据用户和委单ID读取委单历史记录
     *
     * @param userId       用户ID
     * @param entrustId 委单ID
     * @return 历史委单实体对象
     */
    FEntrustHistory getEntrustHistory(Integer userId, BigInteger entrustId);
    
    /**
     * 根据用户和委单ID读取委单历史记录
     *
     * @param userId       用户ID
     * @param entrustId 委单ID
     * @return 历史委单实体对象
     */
    FEntrustHistory getEntrustHistoryByEntrustId(Integer userId, BigInteger entrustId);
}
