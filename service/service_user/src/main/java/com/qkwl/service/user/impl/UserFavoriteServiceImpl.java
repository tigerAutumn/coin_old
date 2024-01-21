package com.qkwl.service.user.impl;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.qkwl.common.dto.Enum.FavoriteOperateTypeEnum;
import com.qkwl.common.dto.user.FUserFavoriteTrade;
import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.rpc.user.IUserFavoriteService;
import com.qkwl.service.user.dao.FUserFavoriteTradeMapper;

/**
 * 用户业务实现
 *
 * @author ZKF
 */
@Service("userFavoriteService")
public class UserFavoriteServiceImpl implements IUserFavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(UserFavoriteServiceImpl.class);

    @Autowired
    private FUserFavoriteTradeMapper userFavoriteTradeMapper;

	@Override
	public JSONArray updateFavoriteByUid(Integer uid, Integer tradeid, FavoriteOperateTypeEnum favoriteOperateTypeEnum,String fFavoriteTradeList){
		logger.info("start updateFavoriteByUid");
		try {
			JSONArray parseArray = null;
			if(StringUtils.isEmpty(fFavoriteTradeList)) {
				parseArray = new JSONArray();
			}else {
				parseArray = JSON.parseArray(fFavoriteTradeList);
			}
			if(favoriteOperateTypeEnum.getCode() == FavoriteOperateTypeEnum.ADD.getCode()) {
				if(parseArray.contains(tradeid)) {
					return parseArray;
				}else {
					parseArray.add(tradeid);
				}
			}else if(favoriteOperateTypeEnum.getCode() == FavoriteOperateTypeEnum.REMOVE.getCode()) {
				int indexOf = parseArray.indexOf(tradeid);
				if(indexOf == -1) {
					return parseArray;
				}else {
					parseArray.remove(indexOf);
				}
			}
			FUserFavoriteTrade fUserFavoriteTrade = new FUserFavoriteTrade();
			fUserFavoriteTrade.setFuid(uid);
			fUserFavoriteTrade.setFfavoritetradelist(parseArray.toJSONString());
			if(StringUtils.isEmpty(fFavoriteTradeList)){
				fUserFavoriteTrade.setFcreatetime(new Date());
				int insert = userFavoriteTradeMapper.insert(fUserFavoriteTrade);
				if(insert <= 0) {
					logger.error("updateFavoriteByUid 执行错误，参数：{fFavoriteTradeList："+fFavoriteTradeList+",uid:"+uid+",favoriteOperateTypeEnum:"+favoriteOperateTypeEnum.getCode()+",fFavoriteTradeList:"+fFavoriteTradeList+"}");
				}
			}else{
				int updateByPrimaryKeySelective = userFavoriteTradeMapper.updateByUidSelective(fUserFavoriteTrade);
				if(updateByPrimaryKeySelective <= 0) {
					logger.error("updateFavoriteByUid 执行错误，参数：{fFavoriteTradeList："+fFavoriteTradeList+",uid:"+uid+",favoriteOperateTypeEnum:"+favoriteOperateTypeEnum.getCode()+",fFavoriteTradeList:"+fFavoriteTradeList+"}");
				}
			}
			logger.info("---------------return---------------");
			logger.info(parseArray.toJSONString());
			return parseArray;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public FUserFavoriteTrade selectByUid(Integer uid) {
		return userFavoriteTradeMapper.selectByUid(uid);
	}
    
}
