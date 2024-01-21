package com.qkwl.service.user.dao;

import org.apache.ibatis.annotations.Mapper;

import com.qkwl.common.dto.user.FUserFavoriteTrade;

@Mapper
public interface FUserFavoriteTradeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FUserFavoriteTrade record);

    int insertSelective(FUserFavoriteTrade record);

    FUserFavoriteTrade selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FUserFavoriteTrade record);

    int updateByPrimaryKey(FUserFavoriteTrade record);
    
    
    FUserFavoriteTrade selectByUid(Integer uid);
    
    int updateByUidSelective(FUserFavoriteTrade record);
}