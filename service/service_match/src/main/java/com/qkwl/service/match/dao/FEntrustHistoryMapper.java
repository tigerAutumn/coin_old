package com.qkwl.service.match.dao;

import com.qkwl.common.dto.entrust.FEntrustHistory;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FEntrustHistoryMapper {
	
	/**
	 * 插入
	 * @param record 历史委单
	 * @return 1 or 0
	 */
    int insert(FEntrustHistory record);
    
    /**
     * 批量插入
     */
    int insertBatch(@Param("historyList")List<FEntrustHistory> historyList);
}