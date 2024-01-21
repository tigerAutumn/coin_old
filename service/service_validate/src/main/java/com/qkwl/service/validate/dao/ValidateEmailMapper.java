package com.qkwl.service.validate.dao;

import com.qkwl.service.validate.model.ValidateEmailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ValidateEmailMapper {

    int insert(ValidateEmailDO record);

    ValidateEmailDO selectByPrimaryKey(Integer id);

    int updateByPrimaryKey(ValidateEmailDO record);
}