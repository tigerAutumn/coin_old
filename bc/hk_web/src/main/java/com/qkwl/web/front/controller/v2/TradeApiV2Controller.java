package com.qkwl.web.front.controller.v2;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import com.qkwl.common.dto.Enum.EntrustStateEnum;
import com.qkwl.common.dto.Enum.SystemTradeStatusEnum;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.entrust.FEntrust;
import com.qkwl.common.dto.entrust.FEntrustHistory;
import com.qkwl.common.dto.entrust.FEntrustLog;
import com.qkwl.common.dto.market.TickerData;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.redis.RedisConstant;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.capital.IUserWalletService;
import com.qkwl.common.rpc.entrust.EntrustHistoryService;
import com.qkwl.common.rpc.entrust.IEntrustServer;
import com.qkwl.common.rpc.push.PushService;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.common.util.Utils;
import com.qkwl.web.front.controller.base.JsonBaseController;
import com.qkwl.web.utils.WebConstant;

/**
 * 订单相关接口，包括：下单、撤单、获取订单列表、当前订单、历史订单等。
 */
@Controller
public class TradeApiV2Controller extends JsonBaseController {

    private static final Logger logger = LoggerFactory.getLogger(TradeApiV2Controller.class);

    @Autowired
    private RedisHelper redisHelper;
    
    @Autowired
    private PushService pushService;
    
    @Autowired
    private IUserWalletService userWalletService;
    
    @Autowired
    private EntrustHistoryService entrustHistoryService;
    
    @Autowired
    private IEntrustServer entrustServer;
    
    // 买卖盘，最新成交
    @RequestMapping(value = "/v2/collect/list")
    @ResponseBody
    public ReturnResult MarketJsons() throws Exception {
		FUser user = getUser();
		if (user == null) {
            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
        }
		
		if(!StringUtils.isEmpty(user.getfFavoriteTradeList())) {
			JSONArray parseArray = JSON.parseArray(user.getfFavoriteTradeList());
			List<SystemTradeType> tradeTypeSort = redisHelper.getTradeTypeShareByTradeIds(parseArray, WebConstant.BCAgentId);
			
			JSONArray array = new JSONArray();
			for(SystemTradeType systemTradeType:tradeTypeSort) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("tradeId", systemTradeType.getId());
				jsonObject.put("id", systemTradeType.getId());
				jsonObject.put("buySymbol", systemTradeType.getBuyShortName());
				jsonObject.put("sellSymbol", systemTradeType.getSellShortName());
				jsonObject.put("image", systemTradeType.getSellWebLogo());
				
				 // 最新价格
	            TickerData tickerData = pushService.getTickerData(systemTradeType.getId());
	            if (tickerData == null) {
	                jsonObject.put("p_new", 0);
	                jsonObject.put("p_open", 0);
	            } else {
	                jsonObject.put("p_new", tickerData.getLast());
	                jsonObject.put("p_open", tickerData.getKai());
	            }
	            
				//BTC交易区
	            if(systemTradeType.getType().equals(1)) {
					jsonObject.put("cny", tickerData.getLast());
				}
	            else if(systemTradeType.getType().equals(2)) {
					BigDecimal cny = getCny(8, tickerData.getLast());
					jsonObject.put("cny", cny);
				}
				//ETH交易区
				else if (systemTradeType.getType().equals(3)) {
					BigDecimal cny = getCny(11, tickerData.getLast());
					jsonObject.put("cny", cny);
				}
				else if(systemTradeType.getType().equals(4)) {
					BigDecimal cny = getCny(57, tickerData.getLast());
					jsonObject.put("cny", cny);
				}
	            
	            jsonObject.put("total", tickerData.getVol() == null ? 0d : tickerData.getVol());
	            jsonObject.put("buy", tickerData.getBuy() == null ? 0d : tickerData.getLow());
	            jsonObject.put("sell", tickerData.getSell() == null ? 0d : tickerData.getHigh());
	            jsonObject.put("rose", tickerData.getChg() == null ? 0d : tickerData.getChg());
	            array.add(jsonObject);
			}
			return ReturnResult.SUCCESS(array);
		}else {
			//如果是空的则返回空的币种列表
			List<SystemTradeType> tradeTypeSort = new ArrayList<>();
			return ReturnResult.SUCCESS(tradeTypeSort);
		}
    }
    
    public BigDecimal getCny(int tradeId,BigDecimal p_new) {
		//取BTC/GSET交易对价格计算
    	BigDecimal cny = redisHelper.getLastPrice(tradeId);
		
		//当前交易对最新价格
		BigDecimal money = MathUtils.mul(p_new, cny);
		BigDecimal newMoney = MathUtils.toScaleNum(money, 2);
		return newMoney;
	}
    
    @ResponseBody
    @RequestMapping("/v2/trademarket")
    public ReturnResult tradeMarket(
            @RequestParam(required = true, defaultValue = "0") Integer tradeId,
            @RequestParam(value = "type", required = false, defaultValue = "1") Integer type
    ) throws Exception {
        JSONObject jsonObject = new JSONObject();
        SystemTradeType systemTradeType = redisHelper.getTradeType(tradeId, WebConstant.BCAgentId);
        if (systemTradeType == null || systemTradeType.getStatus().equals(SystemTradeStatusEnum.ABNORMAL.getCode())
                || !systemTradeType.getIsShare()) {
            systemTradeType = redisHelper.getTradeTypeFirst(type, WebConstant.BCAgentId);
            if (systemTradeType == null) {
                return ReturnResult.FAILUER("");
            }
            tradeId = systemTradeType.getId();
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
            
            List<UserCoinWallet> userCoinWallets = userWalletService.listUserCoinWallet(fuser.getFid());
            Iterator<UserCoinWallet> iterator = userCoinWallets.iterator();
            while (iterator.hasNext()) {
                UserCoinWallet wallet = (UserCoinWallet) iterator.next();
                if (!redisHelper.hasCoinId(wallet.getCoinId())) {
                    iterator.remove();
                }
            }
            BigDecimal totalAssets = getTotalAssets(userCoinWallets);
            BigDecimal btcPrice = redisHelper.getLastPrice(8);
            BigDecimal btcAssets = MathUtils.div(totalAssets, btcPrice);
            
            jsonObject.put("netAssets", getNetAssets(userCoinWallets));
            jsonObject.put("totalAssets", totalAssets);
            jsonObject.put("btcAssets", btcAssets);
        } else {
            jsonObject.put("login", false);
        }
        
        jsonObject.put("isPlatformStatus", systemTradeType.getStatus() == SystemTradeStatusEnum.NORMAL.getCode());
//        jsonObject.put("coinInfo",redisHelper.getCoinInfo(systemTradeType.getSellCoinId(),super.getLanEnum().getCode()+""));
        return ReturnResult.SUCCESS(jsonObject);
    }
    
    
    //k线交易页委单和资产
    @ResponseBody
    @RequestMapping(value = "/v2/getEntruts")
    public ReturnResult EntrutsJson(
            @RequestParam(required = false, defaultValue = "0") Integer symbol,
            @RequestParam(required = false, defaultValue = "6") Integer count,
            @RequestParam(required = false) Integer type
    ) throws Exception {
        JSONObject result = new JSONObject();
        FUser fuser = super.getCurrentUserInfoByToken();
        if (fuser == null) {
            return ReturnResult.FAILUER("请登录！");
        }
        SystemTradeType tradeType = redisHelper.getTradeType(symbol, WebConstant.BCAgentId);
        if (tradeType == null) {
            return ReturnResult.FAILUER("交易错误！");
        }
        SystemCoinType buyCoinType = pushService.getSystemCoinType(tradeType.getBuyCoinId());
        SystemCoinType sellCoinType = pushService.getSystemCoinType(tradeType.getSellCoinId());
        if (buyCoinType == null || sellCoinType == null) {
            return ReturnResult.FAILUER("币种错误！");
        }
        try {
            List<Integer> stateList = new ArrayList<>();
            // 未成交前6条
            FEntrust curEntrust = new FEntrust();
            curEntrust.setFuid(fuser.getFid());
            curEntrust.setFtradeid(symbol);
            curEntrust.setFagentid(fuser.getFagentid());
            curEntrust.setFtype(type);
            stateList.add(EntrustStateEnum.Going.getCode());
            stateList.add(EntrustStateEnum.PartDeal.getCode());
            stateList.add(EntrustStateEnum.WAITCancel.getCode());
            Pagination<FEntrust> curParam = new Pagination<>(0, count);
            curParam = this.entrustServer.listEntrust(curParam, curEntrust, stateList);
            JSONArray entrutsCur = new JSONArray();
            for (FEntrust fEntrust : curParam.getData()) {
                JSONObject entruts = new JSONObject();
                entruts.put("id", fEntrust.getFid());
                entruts.put("time", Utils.dateFormat(new Timestamp(fEntrust.getFcreatetime().getTime())));
                entruts.put("types", super.GetR18nMsg("trade.enum.entrusttype" + fEntrust.getFtype()));
                entruts.put("source", fEntrust.getFsource_s());
                entruts.put("price", fEntrust.getFprize());
                entruts.put("count", fEntrust.getFcount());

                entruts.put("amount", fEntrust.getFamount());        //总价
                entruts.put("ncount", fEntrust.getFleftcount());  //未成交量

                entruts.put("leftcount", MathUtils.sub(fEntrust.getFcount(), fEntrust.getFleftcount()));
                entruts.put("last", fEntrust.getFlast());
                entruts.put("successamount", fEntrust.getFsuccessamount());
                entruts.put("fees", fEntrust.getFfees());
                entruts.put("status", super.GetR18nMsg("trade.enum.entruststate" + fEntrust.getFstatus()));
                entruts.put("type", fEntrust.getFtype());
                entruts.put("buysymbol", buyCoinType.getShortName());
                entruts.put("sellsymbol", sellCoinType.getShortName());
                entrutsCur.add(entruts);
            }
            result.put("entrutsCur", entrutsCur);
            // 成交前6条
            stateList.clear();
            stateList.add(EntrustStateEnum.AllDeal.getCode());
            stateList.add(EntrustStateEnum.Cancel.getCode());
            FEntrustHistory hisEntrust = new FEntrustHistory();
            hisEntrust.setFuid(fuser.getFid());
            hisEntrust.setFtradeid(symbol);
            hisEntrust.setFagentid(fuser.getFagentid());
            hisEntrust.setFtype(type);
            Pagination<FEntrustHistory> hisParam = new Pagination<>(0, count);
            hisParam = this.entrustHistoryService.listEntrustHistory(hisParam, hisEntrust, stateList);
            JSONArray entrutsHis = new JSONArray();
            for (FEntrustHistory fEntrust : hisParam.getData()) {
                JSONObject entruts = new JSONObject();
                entruts.put("id", fEntrust.getFid());
                entruts.put("time", Utils.dateFormat(new Timestamp(fEntrust.getFcreatetime().getTime())));
                entruts.put("types", super.GetR18nMsg("trade.enum.entrusttype" + fEntrust.getFtype()));
                entruts.put("source", fEntrust.getFsource_s());
                entruts.put("price", fEntrust.getFprize());
                entruts.put("count", fEntrust.getFcount());

                entruts.put("amount", fEntrust.getFamount());        //总价
                entruts.put("ncount", fEntrust.getFleftcount());  //未成交量

                entruts.put("leftcount", MathUtils.sub(fEntrust.getFcount(), fEntrust.getFleftcount()));
                entruts.put("last", fEntrust.getFlast());
                entruts.put("successamount", fEntrust.getFsuccessamount());
                entruts.put("fees", fEntrust.getFfees());
                entruts.put("status", super.GetR18nMsg("trade.enum.entruststate" + fEntrust.getFstatus()));
                entruts.put("type", fEntrust.getFtype());
                entruts.put("buysymbol", buyCoinType.getShortName());
                entruts.put("sellsymbol", sellCoinType.getShortName());
                entrutsHis.add(entruts);
            }
            result.put("entrutsHis", entrutsHis);
            return ReturnResult.SUCCESS("获取成功！", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResult.FAILUER("数据获取失败！");
        }
    }
    
    /**
     * 批量撤单
     * 2018年9月17日
     * web端新增批量撤单接口
     */
    @ResponseBody
    @RequestMapping(value = "/v2/batch_cny_cancel")
    public ReturnResult cancelEntrusts(@RequestParam(required = true) String ids,
    		@RequestParam(required = true) int type) {
        FUser fuser = super.getCurrentUserInfoByToken();
        try {
        	if(StringUtils.isNotEmpty(ids)) {
        		String idsArr[] = ids.split(",");
        		List<String> list = Arrays.asList(idsArr);
        		Result result = entrustServer.cancelEntrustBatch(fuser.getFid(), list ,type);
                if (result.getSuccess()) {
                    return ReturnResult.SUCCESS(super.GetR18nMsg("trade.cancel.order." + result.getCode()));
                } else if (result.getCode().equals(Result.PARAM)) {
                    logger.error("tradeCoinBuy is param error, {}", result.getMsg());
                } else if (result.getCode() < 10000) {
                    return ReturnResult.FAILUER(super.GetR18nMsg("trade.cancel.order." + result.getCode().toString(), result.getData().toString()));
                } else {
                    return ReturnResult.FAILUER(super.GetR18nMsg("com.error." + result.getCode().toString(), result.getData().toString()));
                }
        	}
        } catch (Exception e) {
            logger.error("cancelEntrust is error ", e);
        }
        return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10000"));
    }
    
    
    //订单-当前委单列表
    @ResponseBody
    @RequestMapping(value = "/v2/curEntrutList")
    public ReturnResult curEntrutList(
            @RequestParam(required = false) Integer tradeId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) throws Exception {
        FUser fuser = super.getCurrentUserInfoByToken();
        if (fuser == null) {
            return ReturnResult.FAILUER("请登录！");
        }
        
        try {
            List<Integer> stateList = new ArrayList<>();
            // 未成交前6条
            FEntrust curEntrust = new FEntrust();
            curEntrust.setFuid(fuser.getFid());
            curEntrust.setFtradeid(tradeId);
            curEntrust.setFagentid(fuser.getFagentid());
            curEntrust.setFtype(type);
            stateList.add(EntrustStateEnum.Going.getCode());
            stateList.add(EntrustStateEnum.PartDeal.getCode());
            stateList.add(EntrustStateEnum.WAITCancel.getCode());
            Pagination<FEntrust> curParam = new Pagination<>(page, pageSize);
            curParam.setBegindate(startTime);
            curParam.setEnddate(endTime);
            curParam.setRedirectUrl("https://www.hotcoin.top");
            curParam = this.entrustServer.listEntrust(curParam, curEntrust, stateList);
            JSONArray entrutsCur = new JSONArray();
            
            if(curParam != null && curParam.getData() != null && curParam.getData().size()>0) {
            	for (FEntrust fEntrust : curParam.getData()) {
                    JSONObject entruts = new JSONObject();
                    entruts.put("id", fEntrust.getFid());
                    entruts.put("time", Utils.dateFormat(new Timestamp(fEntrust.getFcreatetime().getTime())));
                    entruts.put("types", super.GetR18nMsg("trade.enum.entrusttype" + fEntrust.getFtype()));
                    entruts.put("source", fEntrust.getFsource_s());
                    entruts.put("price", fEntrust.getFprize());
                    entruts.put("count", fEntrust.getFcount());

                    entruts.put("amount", fEntrust.getFamount());        //总价
                    entruts.put("ncount", fEntrust.getFleftcount());  //未成交量

                    entruts.put("leftcount", MathUtils.sub(fEntrust.getFcount(), fEntrust.getFleftcount()));
                    entruts.put("last", fEntrust.getFlast());
                    entruts.put("successamount", fEntrust.getFsuccessamount());
                    entruts.put("fees", fEntrust.getFfees());
                    entruts.put("status", super.GetR18nMsg("trade.enum.entruststate" + fEntrust.getFstatus()));
                    entruts.put("type", fEntrust.getFtype());
                    
                    SystemCoinType buyCoinType = pushService.getSystemCoinType(fEntrust.getFbuycoinid());
                    SystemCoinType sellCoinType = pushService.getSystemCoinType(fEntrust.getFsellcoinid());
                    
                    entruts.put("buysymbol", buyCoinType.getShortName());
                    entruts.put("sellsymbol", sellCoinType.getShortName());
                    entrutsCur.add(entruts);
                }
            }
            
            JSONObject result = new JSONObject();
            result.put("count", curParam.getTotalRows());
            result.put("list", entrutsCur);
            return ReturnResult.SUCCESS("获取成功！", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResult.FAILUER("数据获取失败！");
        }
    }
    
    //订单-历史委单列表
    @ResponseBody
    @RequestMapping(value = "/v2/historyEntrutList")
    public ReturnResult historyEntrutList(
            @RequestParam(required = false) Integer tradeId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer status
    ) throws Exception {
        FUser fuser = super.getCurrentUserInfoByToken();
        if (fuser == null) {
            return ReturnResult.FAILUER("请登录！");
        }
        try {
            // 成交前6条
        	List<Integer> stateList = new ArrayList<>();
        	if(status != null && status > 0) {
        		stateList.add(status);
        	}else {
        		stateList = null;
        	}
        	
            FEntrustHistory hisEntrust = new FEntrustHistory();
            hisEntrust.setFuid(fuser.getFid());
            hisEntrust.setFtradeid(tradeId);
            hisEntrust.setFagentid(fuser.getFagentid());
            hisEntrust.setFtype(type);
            Pagination<FEntrustHistory> hisParam = new Pagination<>(page, pageSize);
            hisParam.setBegindate(startTime);
            hisParam.setEnddate(endTime);
            hisParam.setRedirectUrl("https://www.hotcoin.top");
            hisParam = this.entrustHistoryService.listEntrustHistory(hisParam, hisEntrust, stateList);
            JSONArray entrutsHis = new JSONArray();
            
            if(hisParam != null && hisParam.getData()!=null && hisParam.getData().size()>0) {
            	for (FEntrustHistory fEntrust : hisParam.getData()) {
                    JSONObject entruts = new JSONObject();
                    entruts.put("id", fEntrust.getFid());
                    entruts.put("time", Utils.dateFormat(new Timestamp(fEntrust.getFcreatetime().getTime())));
                    entruts.put("types", super.GetR18nMsg("trade.enum.entrusttype" + fEntrust.getFtype()));
                    entruts.put("source", fEntrust.getFsource_s());
                    entruts.put("price", fEntrust.getFprize());
                    entruts.put("count", fEntrust.getFcount());

                    entruts.put("amount", fEntrust.getFamount());        //总价
                    entruts.put("ncount", fEntrust.getFleftcount());  //未成交量

                    entruts.put("leftcount", MathUtils.sub(fEntrust.getFcount(), fEntrust.getFleftcount()));
                    entruts.put("last", fEntrust.getFlast());
                    entruts.put("successamount", fEntrust.getFsuccessamount());
                    entruts.put("fees", fEntrust.getFfees());
                    entruts.put("status", super.GetR18nMsg("trade.enum.entruststate" + fEntrust.getFstatus()));
                    entruts.put("type", fEntrust.getFtype());
                    SystemCoinType buyCoinType = pushService.getSystemCoinType(fEntrust.getFbuycoinid());
                    SystemCoinType sellCoinType = pushService.getSystemCoinType(fEntrust.getFsellcoinid());
                    
                    entruts.put("buysymbol", buyCoinType.getShortName());
                    entruts.put("sellsymbol", sellCoinType.getShortName());
                    
                    List<FEntrustLog> fentrustlogs = entrustServer.getEntrustLog(fEntrust.getFentrustid());
                    if (fentrustlogs != null && fentrustlogs.size() > 0) {
                       JSONArray array = new JSONArray();
                       for(FEntrustLog fEntrustLog : fentrustlogs) {
                    	   JSONObject jsonObject = new JSONObject();
                    	   jsonObject.put("price", fEntrustLog.getFprize());
                    	   jsonObject.put("count", fEntrustLog.getFcount());
                    	   jsonObject.put("amount", fEntrustLog.getFamount());
                    	   jsonObject.put("fee", fEntrust.getFfees());
                    	   jsonObject.put("time", Utils.dateFormat(new Timestamp(fEntrustLog.getFcreatetime().getTime())));
                    	   array.add(jsonObject);
                       }
                       entruts.put("entrustLog", array);
                    }
                    
                    entrutsHis.add(entruts);
                }
            }
            
            JSONObject result = new JSONObject();
            if(hisParam != null) {
            	result.put("count", hisParam.getTotalRows());
            }else {
            	result.put("count", 0);
            }
            
            result.put("list", entrutsHis);
            return ReturnResult.SUCCESS("获取成功！", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResult.FAILUER("数据获取失败！");
        }
    }
    
    //订单-历史委单列表
    @ResponseBody
    @RequestMapping(value = "/v2/entrustDetailList")
    public ReturnResult entrustDetailList(
            @RequestParam(required = false) Integer tradeId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer status
    ) throws Exception {
        FUser fuser = super.getCurrentUserInfoByToken();
        if (fuser == null) {
            return ReturnResult.FAILUER("请登录！");
        }
        try {
            // 成交前6条
        	List<Integer> stateList = new ArrayList<>();
        	if(status != null && status > 0) {
        		stateList.add(status);
        	}else {
        		stateList = null;
        	}
        	
            FEntrustHistory hisEntrust = new FEntrustHistory();
            hisEntrust.setFuid(fuser.getFid());
            hisEntrust.setFtradeid(tradeId);
            hisEntrust.setFagentid(fuser.getFagentid());
            hisEntrust.setFtype(type);
            Pagination<FEntrustHistory> hisParam = new Pagination<>(page, pageSize);
            hisParam.setBegindate(startTime);
            hisParam.setEnddate(endTime);
            hisParam.setRedirectUrl("https://www.hotcoin.top");
            hisParam = this.entrustHistoryService.listEntrustHistory(hisParam, hisEntrust, stateList);
            JSONArray entrutsHis = new JSONArray();
            
            if(hisParam != null && hisParam.getData()!=null && hisParam.getData().size()>0) {
            	for (FEntrustHistory fEntrust : hisParam.getData()) {
                    JSONObject entruts = new JSONObject();
                    entruts.put("id", fEntrust.getFid());
                    entruts.put("time", Utils.dateFormat(new Timestamp(fEntrust.getFcreatetime().getTime())));
                    entruts.put("types", super.GetR18nMsg("trade.enum.entrusttype" + fEntrust.getFtype()));
                    entruts.put("source", fEntrust.getFsource_s());
                    entruts.put("price", fEntrust.getFprize());
                    entruts.put("count", fEntrust.getFcount());

                    entruts.put("amount", fEntrust.getFamount());        //总价
                    entruts.put("ncount", fEntrust.getFleftcount());  //未成交量

                    entruts.put("leftcount", MathUtils.sub(fEntrust.getFcount(), fEntrust.getFleftcount()));
                    entruts.put("last", fEntrust.getFlast());
                    entruts.put("successamount", fEntrust.getFsuccessamount());
                    entruts.put("fees", fEntrust.getFfees());
                    entruts.put("status", super.GetR18nMsg("trade.enum.entruststate" + fEntrust.getFstatus()));
                    entruts.put("type", fEntrust.getFtype());
                    SystemCoinType buyCoinType = pushService.getSystemCoinType(fEntrust.getFbuycoinid());
                    SystemCoinType sellCoinType = pushService.getSystemCoinType(fEntrust.getFsellcoinid());
                    
                    entruts.put("buysymbol", buyCoinType.getShortName());
                    entruts.put("sellsymbol", sellCoinType.getShortName());
                    
                    List<FEntrustLog> fentrustlogs = entrustServer.getEntrustLog(fEntrust.getFentrustid());
                    if (fentrustlogs != null && fentrustlogs.size() > 0) {
                       JSONArray array = new JSONArray();
                       for(FEntrustLog fEntrustLog : fentrustlogs) {
                    	   JSONObject jsonObject = new JSONObject();
                    	   jsonObject.put("price", fEntrustLog.getFprize());
                    	   jsonObject.put("count", fEntrustLog.getFcount());
                    	   jsonObject.put("amount", fEntrustLog.getFamount());
                    	   jsonObject.put("fee", fEntrust.getFfees());
                    	   jsonObject.put("time", Utils.dateFormat(new Timestamp(fEntrustLog.getFcreatetime().getTime())));
                    	   array.add(jsonObject);
                       }
                       entruts.put("entrustLog", array);
                    }
                    
                    entrutsHis.add(entruts);
                }
            }
            
            JSONObject result = new JSONObject();
            if(hisParam != null) {
            	result.put("count", hisParam.getTotalRows());
            }else {
            	result.put("count", 0);
            }
            
            result.put("list", entrutsHis);
            return ReturnResult.SUCCESS("获取成功！", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResult.FAILUER("数据获取失败！");
        }
    }
    
    /**
     * 获取深度数据
     * @param tradeId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/v2/fullDepth")
    public ReturnResult fulldepth(
            @RequestParam(required = false, defaultValue = "0") int tradeId) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject returnObject = new JSONObject();
        if (tradeId == 0) {
            return ReturnResult.FAILUER("币种ID错误！");
        }
        
        SystemTradeType tradeType =  pushService.getSystemTradeType(tradeId);
        if(!tradeType.getStatus().equals(SystemTradeStatusEnum.ABNORMAL.getCode())) {
        	// 小数位处理(默认价格2位，数量4位)
    		String digit = StringUtils.isEmpty(tradeType.getDigit()) ? "2#4" : tradeType.getDigit();
            String[] digits = digit.split("#");
            int cnyDigit = Integer.valueOf(digits[0]);
            int coinDigit = Integer.valueOf(digits[1]);
            
            JSONArray buyDepth = pushService.getBuyDepthJson(tradeId);
            // 数据过滤
            JSONArray newBuyDepth = new JSONArray();
            BigDecimal allCount = BigDecimal.ZERO;
            for (Object object : buyDepth) {
                JSONArray array = JSON.parseArray(object.toString());
                
                JSONArray newArray = new JSONArray();
                if (Double.valueOf(array.get(1).toString()) > 0d) {
                	//单价
                	BigDecimal price = new BigDecimal(array.get(0).toString());
                	//数量
                	BigDecimal count = new BigDecimal(array.get(1).toString());
                	
                	newArray.add(MathUtils.toScaleNum(price,cnyDigit));
                	newArray.add(MathUtils.toScaleNum(count,coinDigit));
                	allCount = MathUtils.add(allCount, count);
                	newArray.add(MathUtils.toScaleNum(allCount,coinDigit));
                	newBuyDepth.add(newArray);
                }
            }
            
            JSONArray sellDepth = pushService.getSellDepthJson(tradeId);
	        List<Object> list = JSONArray.parseArray(sellDepth.toJSONString(), Object.class);
	        // 数据过滤
	        List<Object> newSellDepth = new JSONArray();
	        BigDecimal allSellCount = BigDecimal.ZERO;
	        for (Object object : list) {
	            JSONArray array = JSON.parseArray(object.toString());
	            if (Double.valueOf(array.get(1).toString()) > 0d) {
	            	JSONArray newArray = new JSONArray();
		            if (Double.valueOf(array.get(1).toString()) > 0d) {
		            	//单价
		            	BigDecimal price = new BigDecimal(array.get(0).toString());
		            	//数量
		            	BigDecimal count = new BigDecimal(array.get(1).toString());
		            	
		            	newArray.add(MathUtils.toScaleNum(price,cnyDigit));
		            	newArray.add(MathUtils.toScaleNum(count,coinDigit));
		            	allSellCount = MathUtils.add(allSellCount, count);
		            	newArray.add(MathUtils.toScaleNum(allSellCount,coinDigit));
		            	newSellDepth.add(newArray);
		            }
	            }
	        }
	        sort(newSellDepth);
            jsonObject.put("buyDepth", newBuyDepth);
            jsonObject.put("sellDepth", newSellDepth);
            jsonObject.put("date", Utils.getTimestamp().getTime() / 1000);
            returnObject.put("depth", jsonObject);
        }
        return ReturnResult.SUCCESS(returnObject);
    }
    
    public void sort(List<Object> list) {
		Collections.sort(list, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				JSONArray o1Array = JSON.parseArray(o1.toString());
				JSONArray o2Array = JSON.parseArray(o2.toString());
            	//o1单价
            	BigDecimal o1Price = new BigDecimal(o1Array.get(0).toString());
            	//o2单价
            	BigDecimal o2Price = new BigDecimal(o2Array.get(0).toString());
		            	
            	if(o1Price.compareTo(o2Price)<0) {
            		return 1;
            	}else if(o1Price.compareTo(o2Price)==0) {
            		return 0;
            	}else {
            		return -1;
            	}
			}
		});
	}
    
    
    // 买卖盘，最新成交
    @RequestMapping(value = "/v2/tradeInfo")
    @ResponseBody
    public ReturnResult tradeInfo(int id) throws Exception {
    	List<SystemTradeType> list = redisHelper.getAllTradeTypeList(WebConstant.BCAgentId);
		JSONArray array = new JSONArray();
		for(SystemTradeType systemTradeType:list) {
			if(!systemTradeType.getStatus().equals(SystemTradeStatusEnum.ABNORMAL.getCode())
        			&&systemTradeType.getType().equals(id)) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("tradeId", systemTradeType.getId());
				jsonObject.put("id", systemTradeType.getId());
				jsonObject.put("buySymbol", systemTradeType.getBuyShortName());
				jsonObject.put("sellSymbol", systemTradeType.getSellShortName());
				jsonObject.put("image", systemTradeType.getSellWebLogo());
				
				 // 最新价格
	            TickerData tickerData = pushService.getTickerData(systemTradeType.getId());
	            if (tickerData == null) {
	                jsonObject.put("p_new", 0);
	                jsonObject.put("p_open", 0);
	            } else {
	                jsonObject.put("p_new", tickerData.getLast());
	                jsonObject.put("p_open", tickerData.getKai());
	            }
	            jsonObject.put("total", tickerData.getVol() == null ? 0d : tickerData.getVol());
	            jsonObject.put("buy", tickerData.getBuy() == null ? 0d : tickerData.getLow());
	            jsonObject.put("sell", tickerData.getSell() == null ? 0d : tickerData.getHigh());
	            jsonObject.put("rose", tickerData.getChg() == null ? 0d : tickerData.getChg());
	            array.add(jsonObject);
			}
		}
		return ReturnResult.SUCCESS(array);
    }
    
    // 买卖盘，最新成交
    @RequestMapping(value = "/v2/realTimeTrade")
    @ResponseBody
    public ReturnResult MarketJson(
            @RequestParam(required = false, defaultValue = "0") Integer tradeId,
            @RequestParam(required = false, defaultValue = "0") Integer successCount
    ) throws Exception {
        if (tradeId == 0 || successCount < 0) {
            return ReturnResult.FAILUER("");
        }
        if (successCount > 100 || successCount ==0) {
        	successCount = 100;
        }
        //获取虚拟币
        SystemTradeType tradeType = redisHelper.getTradeType(tradeId, WebConstant.BCAgentId);
        if (tradeType == null || tradeType.getStatus().equals(SystemTradeStatusEnum.ABNORMAL.getCode())) {
            return ReturnResult.FAILUER("");
        }
        
        String digit = StringUtils.isEmpty(tradeType.getDigit()) ? "2#4" : tradeType.getDigit();
        String[] digits = digit.split("#");
        int cnyDigit = Integer.valueOf(digits[0]);
        int coinDigit = Integer.valueOf(digits[1]);
        
        JSONObject jsonObject = new JSONObject();
        String arrayStr = redisHelper.getRedisData(RedisConstant.SUCCESSENTRUST_KEY + tradeId);
        JSONArray successArray = JSONArray.parseArray(arrayStr);
        JSONArray newSuccessArray = new JSONArray();
        for(Object successObj :successArray) {
        	JSONArray array = JSON.parseArray(successObj.toString());
        	JSONArray newArray = new JSONArray();
        	BigDecimal price = new BigDecimal(array.get(0).toString());
        	BigDecimal count = new BigDecimal(array.get(1).toString());
        	newArray.add(MathUtils.toScaleNum(price,cnyDigit));
        	newArray.add(MathUtils.toScaleNum(count,coinDigit));
        	newArray.add(array.get(2));
        	newArray.add(array.get(3));
        	newSuccessArray.add(newArray);
        }
        // 最新成交
        jsonObject.put("trades", newSuccessArray);
        return ReturnResult.SUCCESS(jsonObject);
    }
}
