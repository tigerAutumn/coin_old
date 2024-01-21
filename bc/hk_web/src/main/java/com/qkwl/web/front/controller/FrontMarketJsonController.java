package com.qkwl.web.front.controller;

import com.alibaba.druid.util.StringUtils;
import com.qkwl.common.dto.Enum.SystemTradeStatusEnum;
import com.qkwl.common.dto.Enum.SystemTradeTypeEnum;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.coin.SystemTradeTypeVO;
import com.qkwl.common.dto.news.FArticle;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.web.FSystemLan;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.i18n.LuangeHelper;
import com.qkwl.common.redis.RedisConstant;
import com.qkwl.common.rpc.push.PushService;
import com.qkwl.common.util.ModelMapperUtils;
import com.qkwl.web.front.controller.base.JsonBaseController;
import com.qkwl.web.utils.WebConstant;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.common.util.Utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;


@Controller
public class FrontMarketJsonController extends JsonBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(FrontMarketJsonController.class);

    @Autowired
    private RedisHelper redisHelper;
    
    @Autowired
    private PushService pushService;

    //获取K线数据
    @ResponseBody
    @RequestMapping("/kline/fullperiod")
    public String fullperiod(
            @RequestParam(required = true) int step,
            @RequestParam(required = true) int symbol
    ) throws Exception {
        JSONArray result = pushService.getKlineJson(symbol, step / 60);
        if (result != null) {
            return result.clone().toString();
        }
        return "[[]]";
    }

    @ResponseBody
    @RequestMapping("/kline/fulldepth")
    public ReturnResult fulldepth(
            @RequestParam(required = false, defaultValue = "0") int step,
            @RequestParam(required = false, defaultValue = "0") int symbol
    ) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject returnObject = new JSONObject();
        if (symbol == 0) {
            return ReturnResult.FAILUER("币种ID错误！");
        }
        jsonObject.put("bids", pushService.getBuyDepthJson(symbol));
        jsonObject.put("asks", pushService.getSellDepthJson(symbol));
        jsonObject.put("date", Utils.getTimestamp().getTime() / 1000);
        returnObject.put("depth", jsonObject);
        if (step > 0) {
            JSONObject periodobject = new JSONObject();
            periodobject.put("marketFrom", symbol);
            periodobject.put("coinVol", symbol);
            periodobject.put("type", step);
            periodobject.put("data", pushService.getLastKlineJson(symbol, step / 60));
            returnObject.put("period", periodobject);
        }
        return ReturnResult.SUCCESS(returnObject);
    }

    @ResponseBody
    @RequestMapping("/kline/lastperiod")
    public ReturnResult lastPeriod(
            @RequestParam(required = false, defaultValue = "0") int step,
            @RequestParam(required = false, defaultValue = "0") int symbol
    ) throws Exception {
        if (symbol == 0) {
            return ReturnResult.FAILUER("币种ID错误！");
        }
        return ReturnResult.SUCCESS(pushService.getLastKlineJson(symbol, step / 60));
    }
    
    @ResponseBody
    @RequestMapping("/market/rate")
    public ReturnResult exchangeRate() throws Exception {
    	try {
	        String cnyValue = getCNYValue();
	        if (!TextUtils.isEmpty(cnyValue)) {
	            JSONObject jsonObject = new JSONObject();
	            jsonObject.put("CNY", cnyValue);
	            return ReturnResult.SUCCESS(jsonObject);
	        }
        } catch (Exception e) {
            logger.error("请求 /market/rate 异常" , e);
        }
        return ReturnResult.FAILUER("请求出错");
    }

    @ResponseBody
    @RequestMapping("/trademarket_json")
    public ReturnResult tradeMarket(
            @RequestParam(value = "symbol", required = false, defaultValue = "0") Integer symbol,
            @RequestParam(value = "type", required = false, defaultValue = "1") Integer tradeType,
            @RequestParam(value = "sb", required = false, defaultValue = "btc_usdt") String sellBuy,
            @RequestParam(value = "limit", required = false, defaultValue = "0") Integer limit
    ) throws Exception {
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
        limit = limit < 0 ? 0 : limit;
        limit = limit > 1 ? 1 : limit;
        JSONObject jsonObject = new JSONObject();
        SystemTradeType systemTradeType = redisHelper.getTradeType(symbol, WebConstant.BCAgentId);
        if (systemTradeType == null || systemTradeType.getStatus().equals(SystemTradeStatusEnum.ABNORMAL.getCode())
                || !systemTradeType.getIsShare()) {
            systemTradeType = redisHelper.getTradeTypeFirst(tradeType, WebConstant.BCAgentId);
            if (systemTradeType == null) {
                return ReturnResult.FAILUER("");
            }
            symbol = systemTradeType.getId();
        }
        // 小数位处理(默认价格2位，数量4位)
        String digit = StringUtils.isEmpty(systemTradeType.getDigit()) ? "2#4" : systemTradeType.getDigit();
        String[] digits = digit.split("#");
        jsonObject.put("cnyDigit", Integer.valueOf(digits[0]));
        jsonObject.put("coinDigit", Integer.valueOf(digits[1]));
        FUser fuser = getCurrentUserInfoByToken();
        if (fuser != null) {
            jsonObject.put("isTelephoneBind", fuser.getFistelephonebind());
            jsonObject.put("tradePassword", fuser.getFtradepassword() == null);
            jsonObject.put("needTradePasswd", redisHelper.getNeedTradePassword(fuser.getFid()));
            jsonObject.put("login", true);
        } else {
            jsonObject.put("login", false);
        }

        //现有的交易区
        Map<Integer, Object> typeMap = new LinkedHashMap<>();

        //交易区对应的交易对
        Map<Integer, List<SystemTradeTypeVO>> tradeTypeListMap = new LinkedHashMap<>();

        //所有交易对
        List<SystemTradeType> tradeTypeList = redisHelper.getTradeTypeList(WebConstant.BCAgentId);

        for (SystemTradeTypeEnum typeEnum : SystemTradeTypeEnum.values()) {
            typeMap.put(typeEnum.getCode(), typeEnum.getSymbol());
            List<SystemTradeType> tempTradeTypeList = new ArrayList<>();
            for (SystemTradeType stt : tradeTypeList) {
                if (stt.getType() == typeEnum.getCode()) {
                    stt.setBuyShortName(stt.getBuyShortName().toLowerCase());
                    stt.setSellShortName(stt.getSellShortName().toLowerCase());
                    tempTradeTypeList.add(stt);
                }
            }
            tradeTypeListMap.put(typeEnum.getCode(), ModelMapperUtils.mapper(tempTradeTypeList, SystemTradeTypeVO.class));
        }

        //List<SystemTradeTypeVO> mapper = ModelMapperUtils.mapper(redisHelper.getTradeTypeList(WebConstant.BCAgentId), SystemTradeTypeVO.class);
        redisHelper.getCoinInfo(systemTradeType.getSellCoinId(),super.getLanEnum().getName());

        //英文的交易名称
        sellBuy = sellBuy.toLowerCase();
        //交易 卖买
        jsonObject.put("sb", sellBuy);
        //交易 卖方
        jsonObject.put("sell", sellBuy.split("_")[0]);
        //交易 买方
        jsonObject.put("buy", sellBuy.split("_")[1]);
        jsonObject.put("typeMap", typeMap);
        jsonObject.put("type", tradeType);
        jsonObject.put("tradeType", ModelMapperUtils.mapper(systemTradeType, SystemTradeTypeVO.class));
        List<FArticle> farticles = redisHelper.getArticles(systemLan.getFid() ,2, 2, 1, WebConstant.BCAgentId);
        jsonObject.put("article", farticles);    //公告

        jsonObject.put("symbol", symbol);
        jsonObject.put("tradeTypeListMap", tradeTypeListMap);
        jsonObject.put("isPlatformStatus", systemTradeType.getStatus() == SystemTradeStatusEnum.NORMAL.getCode());
        jsonObject.put("limit", limit);
        jsonObject.put("coinInfo",redisHelper.getCoinInfo(systemTradeType.getSellCoinId(),super.getLanEnum().getCode()+""));
        return ReturnResult.SUCCESS(jsonObject);

    }
}
