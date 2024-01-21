package com.qkwl.service.score.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * 钱包充值提现记录数据操作接口
 */
@Mapper
public interface FWalletCapitalOperationMapper {
    
    /**
     * 分页查询记录的记录数
     * @param map
     * @return
     */
    int countWalletCapitalOperation(Map<String,Object> map);

}