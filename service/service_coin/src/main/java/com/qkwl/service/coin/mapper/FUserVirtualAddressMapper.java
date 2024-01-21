package com.qkwl.service.coin.mapper;

import com.qkwl.common.dto.capital.FUserVirtualAddressDTO;
import com.qkwl.common.dto.coin.FPool;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户虚拟币地址数据操作接口
 */
@Mapper
public interface FUserVirtualAddressMapper {

	/**
	 * 根据用户和币种查询提现地址 
	 */
	FUserVirtualAddressDTO selectByCoinAndAddress(@Param("fcoinid") int fcoinid, @Param("fadderess") String fadderess);
	
    List<FUserVirtualAddressDTO> selectByCoinId(Integer fcoinid);
    
    int updateAdressByPrimaryKey(FUserVirtualAddressDTO fUserVirtualAddressDTO);
	
}