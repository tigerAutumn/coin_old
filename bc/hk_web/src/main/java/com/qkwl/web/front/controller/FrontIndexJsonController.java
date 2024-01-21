package com.qkwl.web.front.controller;


import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.dto.Enum.SystemTradeTypeEnum;
import com.qkwl.common.dto.Enum.SystemTradeTypeNewEnum;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.coin.SystemTradeTypeVO;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.model.KeyValues;
import com.qkwl.common.rpc.push.PushService;
import com.qkwl.common.util.ModelMapperUtils;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.web.front.controller.base.JsonBaseController;
import com.qkwl.web.utils.WebConstant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FrontIndexJsonController extends JsonBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(FrontIndexJsonController.class);

    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private PushService pushService;

    @ResponseBody
    @RequestMapping(value = "/index_json")
    public ReturnResult indexs() throws Exception {
        List<SystemTradeType> tradeTypeList = redisHelper.getTradeTypeList(WebConstant.BCAgentId);
        // 交易分类
        Map<Integer, Object> typeMap = new LinkedHashMap<>();

        Map<Integer, String> SymbolMap = new LinkedHashMap<>();
        for (SystemTradeTypeEnum typeEnum : SystemTradeTypeEnum.values()) {
            typeMap.put(typeEnum.getCode(), typeEnum.getValue());
            SymbolMap.put(typeEnum.getCode(), typeEnum.getSymbol());
            //System.out.println(typeEnum.getSymbol());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("typeFirst", SystemTradeTypeEnum.GSET.getCode());
        jsonObject.put("tradeTypeList", ModelMapperUtils.mapper(tradeTypeList, SystemTradeTypeVO.class));
        jsonObject.put("typeMap", typeMap);
        jsonObject.put("SymbolMap", SymbolMap);
        // bank info
        return ReturnResult.SUCCESS(jsonObject);
    }
    
    @ResponseBody
    @RequestMapping(value = "/index_json_new")
    public ReturnResult indexsNew() throws Exception {
    	try {
    		List<SystemTradeType> tradeTypeList = redisHelper.getTradeTypeList(WebConstant.BCAgentId);
            // 交易分类
            Map<Integer, Object> typeMap = new LinkedHashMap<>();
            Map<Integer, String> SymbolMap = new LinkedHashMap<>();
            for (SystemTradeTypeNewEnum typeEnum : SystemTradeTypeNewEnum.values()) {
	                typeMap.put(typeEnum.getCode(), typeEnum.getValue());
	                SymbolMap.put(typeEnum.getCode(), typeEnum.getSymbol());
	                //System.out.println(typeEnum.getSymbol());
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("typeFirst", SystemTradeTypeNewEnum.GSET.getCode());
            jsonObject.put("tradeTypeList", ModelMapperUtils.mapper(tradeTypeList, SystemTradeTypeVO.class));
            jsonObject.put("typeMap", typeMap);
            jsonObject.put("SymbolMap", SymbolMap);
            // bank info
            return ReturnResult.SUCCESS(jsonObject);
		} catch (Exception e) {
			logger.error("请求 /index_json_new 错误",e);
			return ReturnResult.FAILUER("系统异常");
		}
    }

    @ResponseBody
    @RequestMapping(value = "/articles_json")
    public ReturnResult getarticles(@RequestParam(required = false, defaultValue = "1") Integer locale) throws Exception {
        //en,cn,tw
        String localeStr = "zh_TW";
        switch (locale) {
            case 1:
                localeStr = "zh_TW";
                break;  //繁体
            case 2:
                localeStr = "en_US";
                break;
            case 3:
                localeStr = "zh_CN";
                break;
        }

        List<KeyValues> articles = pushService.getArticles(localeStr);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("articles", articles);
        // bank info
        return ReturnResult.SUCCESS(jsonObject);
    }

}
