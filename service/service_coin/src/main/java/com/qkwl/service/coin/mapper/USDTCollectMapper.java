package com.qkwl.service.coin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.qkwl.common.dto.coin.USDTCollect;

@Mapper
public interface USDTCollectMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(USDTCollect record);

    USDTCollect selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(USDTCollect record);

    int updateByPrimaryKey(USDTCollect record);
    
    /**
     * 查询1小时前未收集的usdt地址
     * @param coinType 虚拟币
     * @throws Exception 执行异常
     */
    List<USDTCollect> selectUnCollectList(@Param("start") Integer start,@Param("count") Integer count);

}