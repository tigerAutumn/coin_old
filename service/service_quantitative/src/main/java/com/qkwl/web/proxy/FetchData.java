package com.qkwl.web.proxy;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.Enum.validate.LocaleEnum;
import com.qkwl.common.dto.Enum.SystemTradeStatusEnum;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.market.TickerData;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.rpc.push.PushService;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.web.utils.WebConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.List;

public class FetchData implements IFetchData {

    //@Autowired
    FetchDataProxy fetchDataProxy;

    //@Autowired
    RedisHelper redisHelper;

    @Autowired
    PushService pushService;
    
    @Override
    public ReturnResult MarketJson(Integer symbol, Integer buysellcount, Integer successcount) {
        if (redisHelper.isFetchThirdPlatform()) {
            fetchDataProxy.MarketJson(symbol, buysellcount, successcount);
        }

        if (symbol == 0 || buysellcount < 0 || successcount < 0) {
            return ReturnResult.FAILUER("");
        }
        // 条数限制
        if (buysellcount > 100) {
            buysellcount = 100;
        }
        if (successcount > 100) {
            successcount = 100;
        }
        //获取虚拟币
        SystemTradeType tradeType = redisHelper.getTradeType(symbol, WebConstant.BCAgentId);
        if (tradeType == null || tradeType.getStatus().equals(SystemTradeStatusEnum.ABNORMAL.getCode())) {
            return ReturnResult.FAILUER("");
        }
        JSONObject jsonObject = new JSONObject();
        // 最新价格
        TickerData tickerData = pushService.getTickerData(tradeType.getId());
        if (tickerData == null) {
            jsonObject.put("p_new", 0);
            jsonObject.put("p_open", 0);
        } else {
            jsonObject.put("p_new", tickerData.getLast());
            jsonObject.put("p_open", tickerData.getKai());
        }
        jsonObject.put("total", tickerData.getVol() == null ? 0d : tickerData.getVol());
        jsonObject.put("buy", tickerData.getBuy() == null ? 0d : tickerData.getBuy());
        jsonObject.put("sell", tickerData.getSell() == null ? 0d : tickerData.getSell());
        // 深度
        jsonObject.put("buys", pushService.getBuyDepthJson(tradeType.getId(), buysellcount));
        jsonObject.put("sells", pushService.getSellDepthJson(tradeType.getId(), buysellcount));
        // 最新成交
        jsonObject.put("trades", pushService.getSuccessJson(tradeType.getId(), successcount));
        // symbol
        jsonObject.put("symbol", tradeType.getBuySymbol());
        return ReturnResult.SUCCESS(jsonObject);
    }

    @Override
    public ReturnResult IndexMarketJson(Integer locale) {
        List<SystemTradeType> tradeTypes = redisHelper.getTradeTypeList(WebConstant.BCAgentId);
        if (tradeTypes == null) {
            return ReturnResult.FAILUER("");
        }

        JSONArray array = new JSONArray();
        for (SystemTradeType tradeType : tradeTypes) {
            JSONObject jsonitem = new JSONObject();
            TickerData tickerData = pushService.getTickerData(tradeType.getId());
            jsonitem.put("tradeId", tradeType.getId());
            jsonitem.put("price", tickerData.getLast() == null ? 0d : tickerData.getLast());
            jsonitem.put("total", tickerData.getVol() == null ? 0d : tickerData.getVol());
            jsonitem.put("rose", tickerData.getChg() == null ? 0d : tickerData.getChg());
            jsonitem.put("buy", tickerData.getBuy() == null ? 0d : tickerData.getBuy());
            jsonitem.put("sell", tickerData.getSell() == null ? 0d : tickerData.getSell());
            jsonitem.put("buysymbol", tradeType.getBuySymbol());
            jsonitem.put("sellsymbol", tradeType.getSellSymbol());
            String digit = StringUtils.isEmpty(tradeType.getDigit()) ? "2#4" : tradeType.getDigit();
            String[] digits = digit.split("#");
            Integer cnyDigit = Integer.valueOf(digits[0]);
            Integer coinDigit = Integer.valueOf(digits[1]);
            jsonitem.put("cnyDigit", cnyDigit);
            jsonitem.put("coinDigit", coinDigit);
            jsonitem.put("treadId", tradeType.getId());
            jsonitem.put("sellname", locale.equals(LocaleEnum.EN_US.getCode()) ? tradeType.getSellShortName() : tradeType.getSellName());
            jsonitem.put("image", tradeType.getSellWebLogo());
            jsonitem.put("type", tradeType.getType());
            array.add(jsonitem);
        }
        return ReturnResult.SUCCESS(array);
    }
}
