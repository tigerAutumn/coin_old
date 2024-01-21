package com.qkwl.service.score.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * 虚拟币充值提现记录
 * @author TT
 */
@Mapper
public interface FVirtualCapitalOperationMapper {


    /**
     * 分页查询记录总条数
     * @param map
     * @return
     */
	int countVirtualCapitalOperation(Map<String, Object> map);
}