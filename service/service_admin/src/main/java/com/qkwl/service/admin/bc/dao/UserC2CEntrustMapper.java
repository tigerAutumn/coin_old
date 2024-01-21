package com.qkwl.service.admin.bc.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.qkwl.common.dto.c2c.C2CBusiness;
import com.qkwl.common.dto.c2c.UserC2CEntrust;

@Mapper
public interface UserC2CEntrustMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserC2CEntrust record);

    int insertSelective(UserC2CEntrust record);

    UserC2CEntrust selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserC2CEntrust record);

    int updateByPrimaryKey(UserC2CEntrust record);

    
	/**
	 * 根据条件查询委单
	 * @param UserC2CEntrust 
	 * @return
	 */
	List<UserC2CEntrust> selectByParams(UserC2CEntrust u);

	UserC2CEntrust selectByPrimaryKeyForLock(Integer id);	
	
	/**
	 * 根据userId, coinId统计充提数量
	 * @param UserC2CEntrust 
	 * @return
	 */
	List<UserC2CEntrust> statisticsRechargeWithdrawTotal(@Param("userId")Integer userId,@Param("date")Date date);
}