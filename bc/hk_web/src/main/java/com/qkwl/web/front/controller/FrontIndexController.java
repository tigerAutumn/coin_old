package com.qkwl.web.front.controller;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.qkwl.common.util.AESUtils;
import com.qkwl.common.util.Constant;
import com.qkwl.web.front.controller.base.WebBaseController;
import com.qkwl.web.utils.WebConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.qkwl.common.util.ModelMapperUtils;
import com.qkwl.common.dto.Enum.SystemTradeTypeEnum;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.coin.SystemTradeTypeVO;
import com.qkwl.common.framework.redis.RedisHelper;

@Controller
public class FrontIndexController extends WebBaseController {

    @Autowired
    private RedisHelper redisHelper;

    @RequestMapping("/index")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        List<SystemTradeType> tradeTypeList = redisHelper.getTradeTypeList(WebConstant.BCAgentId);
        modelAndView.addObject("tradeTypeList", ModelMapperUtils.mapper(tradeTypeList, SystemTradeTypeVO.class));
        // 交易分类
        Map<Integer, Object> typeMap = new LinkedHashMap<>();
        for (SystemTradeTypeEnum typeEnum : SystemTradeTypeEnum.values()) {
            typeMap.put(typeEnum.getCode(), typeEnum.getValue());
        }
        modelAndView.addObject("typeFirst", SystemTradeTypeEnum.GSET.getCode());
        modelAndView.addObject("typeMap", typeMap);
        modelAndView.setViewName("front/index");
        return modelAndView;
    }

    /**
     * APP下载页面
     */
    @RequestMapping("dowload/index")
    public ModelAndView Dowload() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("front/dowload/index");
        return modelAndView;
    }

    /**
     * Mobile下载页面
     */
    @RequestMapping("dowload/appshare")
    public ModelAndView Mobile() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("front/dowload/mobile");
        return modelAndView;
    }
}
