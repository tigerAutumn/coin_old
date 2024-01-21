package com.qkwl.service.commission.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemArgsMapper {

	/**
	 * 查询系统参数
	 * @param fkey 键
	 * @return fvalue 值
	 */
    String getFvalue(String fkey);
}
