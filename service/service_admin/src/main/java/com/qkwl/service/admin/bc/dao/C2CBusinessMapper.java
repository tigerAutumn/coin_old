package com.qkwl.service.admin.bc.dao;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.qkwl.common.dto.c2c.C2CBusiness;

@Mapper
public interface C2CBusinessMapper {
	
    /**
     * 查询所有c2c商户
     * @return
     */
	List<C2CBusiness> selectAll();
	
    /**
     * 成交添加
     * @return
     */
	int updateIncrByPrimaryKey(@Param("amount") BigDecimal amount, @Param("id") Integer id);
	
	
    int deleteByPrimaryKey(Integer id);

    int insert(C2CBusiness record);

    int insertSelective(C2CBusiness record);

    C2CBusiness selectByPrimaryKey(Integer id);
    
    C2CBusiness selectByPrimaryKeyForLock(Integer id);

    int updateByPrimaryKeySelective(C2CBusiness record);

    int updateByPrimaryKey(C2CBusiness record);
}