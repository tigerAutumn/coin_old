package com.qkwl.service.validate.dao;

import com.qkwl.service.validate.model.ValidateStatisticsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ValidateStatisticsMapper {

    int insert(ValidateStatisticsDO record);

    ValidateStatisticsDO selectByPrimaryKey(Integer id);

    int updateByPrimaryKey(ValidateStatisticsDO record);
}