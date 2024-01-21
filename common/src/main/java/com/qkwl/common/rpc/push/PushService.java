package com.qkwl.common.rpc.push;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.market.TickerData;
import com.qkwl.common.model.KeyValues;

public interface PushService {

	JSONArray getBuyDepthJson(Integer tradeId);
	
	JSONArray getBuyDepthJson(Integer tradeId, int num);
	
	TickerData getTickerData(Integer tradeId);
	
	JSONArray getSellDepthJson(Integer tradeId);
	
	JSONArray getSellDepthJson(Integer tradeId, int num);
	
	JSONArray getSuccessJson(Integer tradeId);
	
	JSONArray getSuccessJson(Integer tradeId, int num);
	
	JSONArray getKlineJson(Integer tradeId, Integer stepid);
	
	JSONArray getLastKlineJson(Integer tradeId, Integer stepid);
	
	JSONArray getIndexKlineJson(Integer tradeId);
	
	List<KeyValues> getArticles(String lanName);
	
	SystemCoinType getSystemCoinType(Integer coinId);
	
	SystemTradeType getSystemTradeType(Integer tradeId);
}
