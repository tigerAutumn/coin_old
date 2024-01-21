package com.qkwl.service.commission.dao;

import org.apache.ibatis.annotations.Mapper;

import com.qkwl.common.dto.user.FUser;

@Mapper
public interface UserMapper {

	/**
	 * 查询推荐人
	 * @param uid
	 * @return
	 */
	FUser getIntroByUID(Integer uid);
	
	/**
	 * 查询登录名
	 * @param uid
	 * @return
	 */
	FUser getLoginNameByUID(Integer uid);
}
