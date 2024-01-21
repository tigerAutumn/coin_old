package com.qkwl.service.capital.dao.c2c;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.qkwl.common.dto.c2c.C2CBusiness;
@Mapper
public interface C2CBusinessMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(C2CBusiness record);

    int insertSelective(C2CBusiness record);

    C2CBusiness selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(C2CBusiness record);

    int updateByPrimaryKey(C2CBusiness record);
    
    List<C2CBusiness> selectBusinessByType(@Param("type")Integer type,@Param("coinId")Integer coinId);
}