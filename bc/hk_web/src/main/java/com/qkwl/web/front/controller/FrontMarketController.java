package com.qkwl.web.front.controller;

import com.qkwl.web.front.controller.base.WebBaseController;
import com.qkwl.web.utils.WebConstant;
import org.springframework.util.StringUtils;
import com.qkwl.common.dto.Enum.SystemTradeStatusEnum;
import com.qkwl.common.dto.Enum.SystemTradeTypeEnum;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.coin.SystemTradeTypeVO;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.util.ModelMapperUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FrontMarketController extends WebBaseController {

    @Autowired
    private RedisHelper redisHelper;

    @RequestMapping("/kline/fullstart")
    public ModelAndView fullstart(@RequestParam(required = false, defaultValue = "0") Integer symbol) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        SystemTradeType tradeType = redisHelper.getTradeType(symbol, WebConstant.BCAgentId);
        modelAndView.addObject("symbol", tradeType.getId());
        modelAndView.addObject("shortname", tradeType.getSellShortName());
        modelAndView.setViewName("front/market/fullstart");
        return modelAndView;
    }

    @RequestMapping("/trademarket")
    public ModelAndView tradeMarket(
            @RequestParam(value = "symbol", required = false, defaultValue = "0") Integer symbol,
            @RequestParam(value = "type", required = false, defaultValue = "1") Integer tradeType,
            @RequestParam(value = "sb", required = false, defaultValue = "btc_gset") String sellBuy,
            @RequestParam(value = "limit", required = false, defaultValue = "0") Integer limit
    ) throws Exception {
        limit = limit < 0 ? 0 : limit;
        limit = limit > 1 ? 1 : limit;

        ModelAndView modelAndView = new ModelAndView();
        SystemTradeType systemTradeType = redisHelper.getTradeType(symbol, WebConstant.BCAgentId);
        if (systemTradeType == null || systemTradeType.getStatus().equals(SystemTradeStatusEnum.ABNORMAL.getCode())
                || !systemTradeType.getIsShare()) {
            systemTradeType = redisHelper.getTradeTypeFirst(tradeType, WebConstant.BCAgentId);
            if (systemTradeType == null) {
                modelAndView.setViewName("redirect:/");
                return modelAndView;
            }
            symbol = systemTradeType.getId();
        }
        // 小数位处理(默认价格2位，数量4位)
        String digit = StringUtils.isEmpty(systemTradeType.getDigit()) ? "2#4" : systemTradeType.getDigit();
        String[] digits = digit.split("#");
        modelAndView.addObject("cnyDigit", Integer.valueOf(digits[0]));
        modelAndView.addObject("coinDigit", Integer.valueOf(digits[1]));
        FUser fuser = getCurrentUserInfoByToken();
        if (fuser != null) {
            modelAndView.addObject("isTelephoneBind", fuser.getFistelephonebind());
            modelAndView.addObject("tradePassword", fuser.getFtradepassword() == null);
            modelAndView.addObject("needTradePasswd", redisHelper.getNeedTradePassword(fuser.getFid()));
            modelAndView.addObject("login", true);
        } else {
            modelAndView.addObject("login", false);
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

        //英文的交易名称
        sellBuy = sellBuy.toLowerCase();
        //交易 卖买
        modelAndView.addObject("sb", sellBuy);
        //交易 卖方
        modelAndView.addObject("sell", sellBuy.split("_")[0]);
        //交易 买方
        modelAndView.addObject("buy", sellBuy.split("_")[1]);

        modelAndView.addObject("typeMap", typeMap);
        modelAndView.addObject("type", tradeType);
        modelAndView.addObject("tradeType", ModelMapperUtils.mapper(systemTradeType, SystemTradeTypeVO.class));
        modelAndView.addObject("symbol", symbol);
        modelAndView.addObject("tradeTypeListMap", tradeTypeListMap);
        modelAndView.addObject("limit", limit);

        //是否是平台撮合 true平台 否则是火币
        modelAndView.addObject("isPlatformStatus", systemTradeType.getStatus() == SystemTradeStatusEnum.NORMAL.getCode());
        modelAndView.setViewName("front/market/trademarket2");
        return modelAndView;
    }

}
