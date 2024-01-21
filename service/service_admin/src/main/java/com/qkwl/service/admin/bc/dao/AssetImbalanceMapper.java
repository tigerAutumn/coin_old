package com.qkwl.service.admin.bc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.qkwl.common.dto.capital.AssetImbalance;

@Mapper
public interface AssetImbalanceMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AssetImbalance record);

    int insertSelective(AssetImbalance record);

    AssetImbalance selectByPrimaryKey(Integer id);

	/**
	 * 查询记录
	 * @param uid 用户id
	 * @param coinid 币种id
	 * @return 
	 */
    AssetImbalance selectByUidAndCoinId(@Param("uid") Integer uid,@Param("coinid") Integer coinid);
    
	/**
	 * 查询记录
	 * @param AssetImbalance
	 * @return 
	 */
    List<AssetImbalance> selectByParams(AssetImbalance record);
    
	/**
	 * 删除记录
	 * @param uid 用户id
	 * @param coinid 币种id
	 * @return 
	 */
    int deleteByUidAndCoinId(@Param("userId") Integer uid,@Param("coinId") Integer coinid);
    
    int updateByPrimaryKeySelective(AssetImbalance record);

    int updateByPrimaryKey(AssetImbalance record);
}