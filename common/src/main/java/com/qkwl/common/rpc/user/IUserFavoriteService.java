package com.qkwl.common.rpc.user;

import com.alibaba.fastjson.JSONArray;
import com.qkwl.common.dto.Enum.FavoriteOperateTypeEnum;
import com.qkwl.common.dto.user.FUserFavoriteTrade;

/**
 * 用户收藏接口
 * @author hwj
 */
public interface IUserFavoriteService {

	/**
	 * 用户更新
	 * @param uid 
	 * @param tradeid 
	 * @param favoriteOperateTypeEnum 操作，1为添加，0为删除
	 * @param fFavoriteTradeList 修改前的收藏
	 * @return 
	 * @throws Exception
	 */
	JSONArray updateFavoriteByUid(Integer uid,Integer tradeid,FavoriteOperateTypeEnum favoriteOperateTypeEnum,String fFavoriteTradeList);

	
	FUserFavoriteTrade selectByUid(Integer uid);
}
