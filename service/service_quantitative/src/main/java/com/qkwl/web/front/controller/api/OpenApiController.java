package com.qkwl.web.front.controller.api;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.market.TickerData;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.rpc.push.PushService;
import com.qkwl.common.util.RequstLimit;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.web.front.controller.base.JsonBaseController;
import com.qkwl.web.utils.WebConstant;

@Controller
public class OpenApiController extends JsonBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(OpenApiController.class);

	@Autowired
	private RedisHelper redisHelper;
	
	@Autowired
    private PushService pushService;
	
	/**
     * @return
     */
	@RequstLimit
    @ResponseBody
    @RequestMapping("/openApi/ticker")
    public ReturnResult ticker() throws Exception {
        List<SystemTradeType> tradeTypeList = redisHelper.getTradeTypeList(WebConstant.BCAgentId);
        JSONArray array = new JSONArray();
        for (SystemTradeType tradeType : tradeTypeList) 
        {
        	TickerData tickerData = pushService.getTickerData(tradeType.getId());
        	JSONObject jsonObject = new JSONObject();
        	jsonObject.put("symbol", tradeType.getBuyShortName()+"_"+tradeType.getSellShortName());
	        jsonObject.put("last", tickerData.getLast()==null?0d:tickerData.getLast());
	        jsonObject.put("high", tickerData.getHigh()==null?0d:tickerData.getHigh());
	        // 深度
	        jsonObject.put("low", tickerData.getLow()==null?0d:tickerData.getLow());
	        jsonObject.put("vol", tickerData.getVol()==null?0d:tickerData.getVol());
        	array.add(jsonObject);
        }
        return ReturnResult.SUCCESS(array);
    }
	
	 // 买卖盘，最新成交 ，供外部行情网站调用
	 @RequstLimit
	 @ResponseBody
	 @RequestMapping(value = "/v1/market/ticker")
	 public JSONObject marketJsonsTicker() {
		JSONObject jsonObject = new JSONObject();
	 	try {
	      JSONArray jsonArray = new JSONArray();
	      List<SystemTradeType> tradeTypeShare = redisHelper.getTradeTypeShare(0);
	      
	      for (SystemTradeType systemTradeType : tradeTypeShare) {
				if(systemTradeType == null) {
					continue;
				}
				TickerData tickerData = pushService.getTickerData(systemTradeType.getId());
				JSONObject j = new JSONObject();
		        j.put("symbol", systemTradeType.getSellShortName().toLowerCase() + "_" + systemTradeType.getBuyShortName().toLowerCase());
		        j.put("last", tickerData.getLast() == null ? 0d : tickerData.getLast());
		        j.put("high", tickerData.getHigh()== null ? 0d : tickerData.getHigh());
		        j.put("low", tickerData.getLow() == null ? 0d : tickerData.getLow());
		        j.put("vol", tickerData.getVol() == null ? 0d : tickerData.getVol());
		        j.put("buy", tickerData.getBuy() == null ? 0d: tickerData.getBuy());
		        j.put("sell", tickerData.getSell() == null ? 0d : tickerData.getSell());
		        j.put("change", tickerData.getChg() == null ? 0d : tickerData.getChg());
		        jsonArray.add(j);
		  }
	      jsonObject.put("ticker", jsonArray);
	      jsonObject.put("status", "ok");
	 	} catch (Exception e) {
	 	  logger.error("访问/v1/market/ticker异常",e);
	 	  jsonObject.put("ticker", new JSONArray());
	 	  jsonObject.put("status", "error");
		}
	    jsonObject.put("timestamp", new Date().getTime() / 1000);
	    return jsonObject;
	 }
}
