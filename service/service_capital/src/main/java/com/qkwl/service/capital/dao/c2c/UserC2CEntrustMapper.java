package com.qkwl.service.capital.dao.c2c;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.qkwl.common.dto.c2c.UserC2CEntrust;

@Mapper
public interface UserC2CEntrustMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserC2CEntrust record);

    int insertSelective(UserC2CEntrust record);

    UserC2CEntrust selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserC2CEntrust record);

    int updateByPrimaryKey(UserC2CEntrust record);
    
    List<UserC2CEntrust> selectList(UserC2CEntrust record);
}