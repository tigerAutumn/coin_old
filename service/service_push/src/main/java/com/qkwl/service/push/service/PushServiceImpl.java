package com.qkwl.service.push.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.market.TickerData;
import com.qkwl.common.model.KeyValues;
import com.qkwl.common.rpc.push.PushService;
import com.qkwl.service.push.run.AutoCache;
import com.qkwl.service.push.run.AutoMarket;

@Service("pushService")
public class PushServiceImpl implements PushService{

	@Autowired
	private AutoMarket autoMarket;
	
	@Autowired
	private AutoCache autoCache;
	
	@Override
	public JSONArray getBuyDepthJson(Integer tradeId) {
		return autoMarket.getBuyDepthJson(tradeId);
	}

	@Override
	public TickerData getTickerData(Integer tradeId) {
		return autoMarket.getTickerData(tradeId);
	}

	@Override
	public JSONArray getBuyDepthJson(Integer tradeId, int num) {
		return autoMarket.getBuyDepthJson(tradeId, num);
	}

	@Override
	public JSONArray getSellDepthJson(Integer tradeId) {
		return autoMarket.getSellDepthJson(tradeId);
	}

	@Override
	public JSONArray getSellDepthJson(Integer tradeId, int num) {
		return autoMarket.getSellDepthJson(tradeId, num);
	}

	@Override
	public JSONArray getSuccessJson(Integer tradeId) {
		return autoMarket.getSuccessJson(tradeId);
	}

	@Override
	public JSONArray getSuccessJson(Integer tradeId, int num) {
		return autoMarket.getSuccessJson(tradeId, num);
	}

	@Override
	public JSONArray getKlineJson(Integer tradeId, Integer stepid) {
		return autoMarket.getKlineJson(tradeId, stepid);
	}

	@Override
	public JSONArray getLastKlineJson(Integer tradeId, Integer stepid) {
		return autoMarket.getLastKlineJson(tradeId, stepid);
	}

	@Override
	public JSONArray getIndexKlineJson(Integer tradeId) {
		return autoMarket.getIndexKlineJson(tradeId);
	}

	@Override
	public List<KeyValues> getArticles(String lanName) {
		// TODO Auto-generated method stub
		return autoCache.getArticles(lanName);
	}

	@Override
	public SystemCoinType getSystemCoinType(Integer coinId) {
		// TODO Auto-generated method stub
		return autoCache.getSystemCoinType(coinId);
	}

	@Override
	public SystemTradeType getSystemTradeType(Integer tradeId) {
		// TODO Auto-generated method stub
		return autoCache.getSystemTradeType(tradeId);
	}
}
