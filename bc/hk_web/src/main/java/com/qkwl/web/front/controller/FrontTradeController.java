package com.qkwl.web.front.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.qkwl.common.dto.coin.SystemCoinInfo;
import com.qkwl.web.front.controller.base.WebBaseController;
import com.qkwl.web.utils.WebConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.util.StringUtils;
import com.qkwl.common.dto.Enum.SystemTradeStatusEnum;
import com.qkwl.common.util.ModelMapperUtils;
import com.qkwl.common.dto.Enum.EntrustStateEnum;
import com.qkwl.common.dto.Enum.SystemTradeTypeEnum;
import com.qkwl.common.dto.coin.SystemCoinTypeVO;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.coin.SystemTradeTypeVO;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.entrust.FEntrust;
import com.qkwl.common.dto.entrust.FEntrustHistory;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.user.FUserPriceclock;
import com.qkwl.common.rpc.entrust.EntrustHistoryService;
import com.qkwl.common.rpc.entrust.IEntrustServer;
import com.qkwl.common.rpc.push.PushService;
import com.qkwl.common.rpc.user.IUserService;
import com.qkwl.common.framework.redis.RedisHelper;

/**
 * 交易模块
 *
 * @author LY
 */
@Controller
public class FrontTradeController extends WebBaseController {

    private static final Logger logger = LoggerFactory.getLogger(FrontTradeController.class);

    @Autowired
    private IEntrustServer entrustServer;
    @Autowired
    private IUserService userService;
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private PushService pushService;
    @Autowired
    private EntrustHistoryService entrustHistoryService;

    /**
     * 交易页面
     */
    @RequestMapping("/trade/cny_coin")
    public ModelAndView coin(
            @RequestParam(value = "tradeId", required = false, defaultValue = "0") Integer tradeid,
            @RequestParam(value = "type", required = false, defaultValue = "1") Integer tradeType,
            @RequestParam(value = "limit", required = false, defaultValue = "0") Integer limit) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        limit = limit < 0 ? 0 : limit;
        limit = limit > 1 ? 1 : limit;
        // 交易判断
        SystemTradeType systemTradeType = redisHelper.getTradeType(tradeid, WebConstant.BCAgentId);
        if (systemTradeType == null || systemTradeType.getStatus().equals(SystemTradeStatusEnum.ABNORMAL.getCode())
                || !systemTradeType.getIsShare() || !systemTradeType.getType().equals(tradeType)) {
            systemTradeType = redisHelper.getTradeTypeFirst(tradeType, WebConstant.BCAgentId);
            if (systemTradeType == null) {
                modelAndView.setViewName("redirect:/");
            } else {
                modelAndView.setViewName("redirect:/trade/cny_coin.html?tradeId=" + systemTradeType.getId() + "&type="
                        + systemTradeType.getType());
            }
            return modelAndView;
        }
        // 小数位处理(默认价格2位，数量4位)
        String digit = StringUtils.isEmpty(systemTradeType.getDigit()) ? "2#4" : systemTradeType.getDigit();
        String[] digits = digit.split("#");
        modelAndView.addObject("cnyDigit", Integer.valueOf(digits[0]));
        modelAndView.addObject("coinDigit", Integer.valueOf(digits[1]));
        // 用户登录
        FUser fuser = getCurrentUserInfoByToken();
        if (fuser != null) {
            // 是否需要输入交易密码
            fuser = userService.selectUserById(fuser.getFid());
            modelAndView.addObject("needTradePasswd", redisHelper.getNeedTradePassword(fuser.getFid()));
            // 价格闹钟
            FUserPriceclock clock = new FUserPriceclock();
            clock.setFuid(fuser.getFid());
            clock.setFtradeid(tradeid);
            FUserPriceclock priceclock = userService.selectPriceClockByClock(clock);
            modelAndView.addObject("priceclock", priceclock);
            modelAndView.addObject("tradePassword", fuser.getFtradepassword() == null);
            modelAndView.addObject("login", true);

        } else {
            modelAndView.addObject("login", false);
        }
        // 交易分类
        Map<Integer, Object> typeMap = new LinkedHashMap<>();
        for (SystemTradeTypeEnum typeEnum : SystemTradeTypeEnum.values()) {
            typeMap.put(typeEnum.getCode(), typeEnum.getValue());
        }
        modelAndView.addObject("typeMap", typeMap);
        modelAndView.addObject("type", tradeType);
        // 当前种类币种
        Map<Integer, SystemCoinTypeVO> coinMap = new LinkedHashMap<>();
        List<SystemTradeType> tradeTypeSort = redisHelper.getTradeTypeSort(tradeType, WebConstant.BCAgentId);
        for (SystemTradeType SystemTradeType : tradeTypeSort) {
            coinMap.put(SystemTradeType.getId(), ModelMapperUtils
                    .mapper(pushService.getSystemCoinType(SystemTradeType.getSellCoinId()), SystemCoinTypeVO.class));
        }
        SystemCoinInfo coinInfo = redisHelper.getCoinInfo(systemTradeType.getSellCoinId(), getLanEnum().getName());

        modelAndView.addObject("coinInfo", coinInfo);
        modelAndView.addObject("coinMap", coinMap);
        modelAndView.addObject("systemTradeType", ModelMapperUtils.mapper(systemTradeType, SystemTradeTypeVO.class));
        modelAndView.addObject("tradeTypeList",
                ModelMapperUtils.mapper(redisHelper.getTradeTypeList(WebConstant.BCAgentId), SystemTradeTypeVO.class));
        modelAndView.addObject("tradeid", tradeid);
        modelAndView.addObject("limit", limit);
        modelAndView.setViewName("front/trade/trade_coin");
        return modelAndView;
    }

    /**
     * 委单记录
     */
    @RequestMapping("/trade/cny_entrust")
    public ModelAndView entrust(
            @RequestParam(required = false, defaultValue = "0") Integer symbol,
            @RequestParam(required = false, defaultValue = "0") Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage
    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        // 用户
        FUser fuser = getCurrentUserInfoByToken();
        // 虚拟币
        SystemTradeType tradeType = redisHelper.getTradeType(symbol, WebConstant.BCAgentId);
        if (tradeType == null || tradeType.getStatus().equals(SystemTradeStatusEnum.ABNORMAL.getCode()) || !tradeType.getIsShare()) {
            tradeType = redisHelper.getTradeTypeFirst(SystemTradeTypeEnum.BTC.getCode(), WebConstant.BCAgentId);
            if (tradeType == null) {
                modelAndView.setViewName("redirect:/");
            } else {
                modelAndView.setViewName("redirect:/trade/cny_entrust.html?symbol=" + tradeType.getId() + "&status=" + status);
            }
            return modelAndView;
        }
        // 小数位处理(默认价格2位，数量4位)
        String digit = StringUtils.isEmpty(tradeType.getDigit()) ? "2#4" : tradeType.getDigit();
        String[] digits = digit.split("#");
        modelAndView.addObject("cnyDigit", Integer.valueOf(digits[0]));
        modelAndView.addObject("coinDigit", Integer.valueOf(digits[1]));
        List<Integer> StateEnums = new ArrayList<>();
        if (status == 0) {
            // 正在委托
            FEntrust entrust = new FEntrust();
            entrust.setFuid(fuser.getFid());
            entrust.setFtradeid(symbol);
            entrust.setFagentid(fuser.getFagentid());
            StateEnums.add(EntrustStateEnum.Going.getCode());
            StateEnums.add(EntrustStateEnum.PartDeal.getCode());
            Pagination<FEntrust> paginParam = new Pagination<FEntrust>(currentPage, 20, "/trade/cny_entrust.html?symbol=" + symbol + "&status=" + status + "&");
            try {
                paginParam = entrustServer.listEntrust(paginParam, entrust, StateEnums);
                modelAndView.addObject("pagin", paginParam.getPagin());
                modelAndView.addObject("fentrusts", paginParam.getData());
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        } else {
            // 委托完成
            FEntrustHistory entrust = new FEntrustHistory();
            entrust.setFuid(fuser.getFid());
            entrust.setFtradeid(symbol);
            entrust.setFagentid(fuser.getFagentid());
            StateEnums.add(EntrustStateEnum.AllDeal.getCode());
            StateEnums.add(EntrustStateEnum.Cancel.getCode());
            StateEnums.add(EntrustStateEnum.WAITCancel.getCode());
            Pagination<FEntrustHistory> paginParam = new Pagination<FEntrustHistory>(currentPage, 20, "/trade/cny_entrust.html?symbol=" + symbol + "&status=" + status + "&");
            try {
                paginParam = entrustHistoryService.listEntrustHistory(paginParam, entrust, StateEnums);
                modelAndView.addObject("pagin", paginParam.getPagin());
                modelAndView.addObject("fentrusts", paginParam.getData());
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }

        modelAndView.addObject("currentPage", currentPage);
        modelAndView.addObject("status", status);
        modelAndView.addObject("tradeType", ModelMapperUtils.mapper(tradeType, SystemTradeTypeVO.class));
        modelAndView.addObject("type", tradeType.getType());
        modelAndView.addObject("tradeTypeList",
                ModelMapperUtils.mapper(redisHelper.getTradeTypeList(WebConstant.BCAgentId), SystemTradeTypeVO.class));
        modelAndView.addObject("symbol", symbol);
        modelAndView.setViewName("front/trade/trade_entrust");
        return modelAndView;
    }
}
