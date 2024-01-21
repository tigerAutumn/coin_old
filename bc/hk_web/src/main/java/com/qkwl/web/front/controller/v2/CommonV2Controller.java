package com.qkwl.web.front.controller.v2;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.dto.Enum.SystemCoinTypeEnum;
import com.qkwl.common.dto.Enum.SystemTradeStatusEnum;
import com.qkwl.common.dto.Enum.SystemTradeTypeNewEnum;
import com.qkwl.common.dto.coin.SystemCoinInfo;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.coin.SystemCoinTypeVO;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.market.TickerData;
import com.qkwl.common.dto.news.FArticle;
import com.qkwl.common.dto.news.FArticleType;
import com.qkwl.common.dto.system.FSystemBankinfoWithdraw;
import com.qkwl.common.dto.web.FFriendLink;
import com.qkwl.common.dto.web.FSystemLan;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.i18n.LuangeHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.rpc.push.PushService;
import com.qkwl.common.util.ModelMapperUtils;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.common.util.Utils;
import com.qkwl.web.front.controller.base.JsonBaseController;
import com.qkwl.web.front.controller.c2c.C2CController;
import com.qkwl.web.utils.WebConstant;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import qiniu.ip17mon.LocationInfo;
import qiniu.ip17mon.Locator;

@Controller
public class CommonV2Controller extends JsonBaseController {
	private static final Logger logger = LoggerFactory.getLogger(C2CController.class);
	
	@Autowired
    private RedisHelper redisHelper;
    @Autowired
    private PushService pushService;
	
	/**
     * 首页轮播图以及公告
     */
    @ResponseBody
    @RequestMapping("/v2/common/index")
    public ReturnResult businessList() 
    {
    	try{
    		JSONArray imageList = new JSONArray();
    		for(int i = 1;i<=5;i++) {
    			JSONObject object = new JSONObject();
    			String url = redisHelper.getSystemArgs("bigImage"+i);
    			String targetUrl = redisHelper.getSystemArgs("bigImage"+i+"url");
    			object.put("url",url);
    			object.put("targetUrl",targetUrl);
    			imageList.add(object);
    		}
    		
    		HttpServletRequest request = sessionContextUtils.getContextRequest();
    		FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
    		
    		FArticleType farticletype = redisHelper.getArticleType(5, systemLan.getFid());
            if (farticletype == null) {
                return ReturnResult.FAILUER("");
            }
            List<FArticle> farticles = redisHelper.getArticles(systemLan.getFid(),2,farticletype.getFid(),1,WebConstant.BCAgentId);
            JSONArray articleList = new JSONArray();
    		if(farticles != null && farticles.size()>0) {
                for(FArticle fArticle : farticles) {
                	JSONObject object = new JSONObject();
                	object.put("title",fArticle.getFtitle());
        			object.put("id",fArticle.getFid());
        			object.put("typeId",fArticle.getFarticletype());
        			object.put("farticletype_s","官方公告");
        			articleList.add(object);
                }
    		}
            
            JSONObject result = new JSONObject();
            result.put("imageList", imageList);
            result.put("articleList", articleList);
	        return ReturnResult.SUCCESS(result);
		}catch(Exception e){
			e.printStackTrace();
		}
    	return ReturnResult.FAILUER("");
    }
    
    
    /**
     * 根据交易区id查询交易对列表
     */
    @ResponseBody
    @RequestMapping("/v2/common/tradeList")
    public ReturnResult tradeList(@RequestParam(required = true, defaultValue = "0") Integer id) 
    {
    	try{
    		List<SystemTradeType> list = redisHelper.getAllTradeTypeList(WebConstant.BCAgentId);
    		
    		JSONArray array = new JSONArray();
            for(SystemTradeType systemTradeType : list) {
            	if(!systemTradeType.getStatus().equals(SystemTradeStatusEnum.ABNORMAL.getCode())
            			&&systemTradeType.getType().equals(id)) {
            		JSONObject obj = new JSONObject();
            		obj.put("id", systemTradeType.getId());
            		obj.put("sellShortName", systemTradeType.getSellShortName());
            		array.add(systemTradeType);
            	}
            }
	        return ReturnResult.SUCCESS(array);
		}catch(Exception e){
			e.printStackTrace();
		}
    	return ReturnResult.FAILUER("");
    }
    
    
    /**
     * 查询四大交易区总量
     */
    @ResponseBody
    @RequestMapping("/v2/common/tradeVol")
    public ReturnResult tradeVol() 
    {
    	try{
    		List<SystemTradeType> list = redisHelper.getAllTradeTypeList(WebConstant.BCAgentId);
    		BigDecimal btcVol = BigDecimal.ZERO;
    		BigDecimal gsetVol = BigDecimal.ZERO;
    		BigDecimal ethVol = BigDecimal.ZERO;
    		BigDecimal usdtVol = BigDecimal.ZERO;
            for(SystemTradeType systemTradeType : list) {
            	//如果币种没有被禁用
            	if(!systemTradeType.getStatus().equals(SystemTradeStatusEnum.ABNORMAL.getCode())) 
            	{
            		//计算BTC区的交易总量
            		if(systemTradeType.getType().equals(SystemTradeTypeNewEnum.BTC.getCode())) {
            			TickerData tickerData = redisHelper.getTickerData(systemTradeType.getId());
            			btcVol = MathUtils.add(btcVol, tickerData.getVol());
            		}
            		
            		//计算GSET区的交易总量
            		if(systemTradeType.getType().equals(SystemTradeTypeNewEnum.GSET.getCode())) {
            			TickerData tickerData = redisHelper.getTickerData(systemTradeType.getId());
            			gsetVol = MathUtils.add(gsetVol, tickerData.getVol());
            		}
            		
            		//计算ETH区的交易总量
            		if(systemTradeType.getType().equals(SystemTradeTypeNewEnum.ETH.getCode())) {
            			TickerData tickerData = redisHelper.getTickerData(systemTradeType.getId());
            			ethVol = MathUtils.add(ethVol, tickerData.getVol());
            		}
            		
            		//计算USDT区的交易总量
            		if(systemTradeType.getType().equals(SystemTradeTypeNewEnum.USDT.getCode())) {
            			TickerData tickerData = redisHelper.getTickerData(systemTradeType.getId());
            			usdtVol = MathUtils.add(usdtVol, tickerData.getVol());
            		}
            	}
            }
            
            JSONObject obj = new JSONObject();
    		obj.put("btcVol", btcVol);
    		obj.put("usdtVol", usdtVol);
    		obj.put("ethVol", ethVol);
    		obj.put("gsetVol", gsetVol);
	        return ReturnResult.SUCCESS(obj);
		}catch(Exception e){
			e.printStackTrace();
		}
    	return ReturnResult.FAILUER("");
    }
    
    public static void main(String[] args) {
    	
    	
		BigDecimal btcVol = BigDecimal.ZERO;
		BigDecimal gsetVol = BigDecimal.ZERO;
		BigDecimal ethVol = BigDecimal.ZERO;
		BigDecimal usdtVol = BigDecimal.ZERO;
        for(int i = 0;i<10;i++) {
        	//如果币种没有被禁用
        	btcVol = MathUtils.add(btcVol, new BigDecimal(5));
        }
        
        System.out.println(btcVol);
	}
    
    /**
     * 根据币种id查询币种信息
     */
    @ResponseBody
    @RequestMapping("/v2/get_coin_info")
    public ReturnResult coinInfo(@RequestParam(required = true) Integer coinId) 
    {
    	try{
    		SystemCoinInfo coinInfo = redisHelper.getCoinInfo(coinId, getLanEnum().getCode()+"");
    		if(coinInfo == null) {
    			return ReturnResult.FAILUER("");
    		}
    		TickerData tickerData = pushService.getTickerData(coinId);
    		if(tickerData != null) {
        		coinInfo.setChg(tickerData.getChg());
        		coinInfo.setLastPrice(tickerData.getLast());
    		}
	        return ReturnResult.SUCCESS(coinInfo);
		}catch(Exception e){
			logger.error("获取币种信息异常，参数coinId："+coinId,e);
		}
    	return ReturnResult.FAILUER("");
    }
    
    /**
     * 根据币种id查询币种信息
     */
    @ResponseBody
    @RequestMapping("/v2/coin_deposit")
    public ReturnResult coinDeposit() 
    {
    	try{
    		List<SystemCoinTypeVO> mapper = ModelMapperUtils.mapper(
                    redisHelper.getCoinTypeIsRechargeList(SystemCoinTypeEnum.COIN.getCode()), SystemCoinTypeVO.class);
            return ReturnResult.SUCCESS(mapper);
		}catch(Exception e){
			logger.error("查询可充值币种信息异常，参数",e);
		}
    	return ReturnResult.FAILUER("系统异常");
    }
    
    /**
     * 根据币种id查询币种信息
     */
    @ResponseBody
    @RequestMapping("/v2/coin_withdraw")
    public ReturnResult coinWithdraw() 
    {
    	try{
    		List<SystemCoinTypeVO> mapper = ModelMapperUtils.mapper(
                    redisHelper.getCoinTypeIsWithdrawList(SystemCoinTypeEnum.COIN.getCode()), SystemCoinTypeVO.class);
            return ReturnResult.SUCCESS(mapper);
		}catch(Exception e){
			logger.error("查询可提现币种信息异常，参数",e);
		}
    	return ReturnResult.FAILUER("系统异常");
    }
    
    /**
     * 根据简称查询币种信息
     */
    @ResponseBody
    @RequestMapping("/v2/coin_info")
    public ReturnResult getCoinInfoByShortName(@RequestParam(required = true) String shortName) 
    {
    	try{
    		if(StringUtils.isEmpty(shortName)) {
    			return ReturnResult.FAILUER("");
    		}
    		SystemCoinType coinTypeShortName = redisHelper.getCoinTypeShortName(shortName.toUpperCase());
    		if(coinTypeShortName == null) {
    			return ReturnResult.FAILUER("");
    		}
    		SystemCoinInfo coinInfo = redisHelper.getCoinInfo(coinTypeShortName.getId(), getLanEnum().getCode()+"");
    		if(coinInfo == null) {
    			return ReturnResult.FAILUER("");
    		}
    		coinInfo.setLogo(coinTypeShortName.getWebLogo());
    		TickerData tickerData = pushService.getTickerData(coinTypeShortName.getId());
    		if(tickerData != null) {
        		coinInfo.setChg(tickerData.getChg());
        		coinInfo.setLastPrice(tickerData.getLast());
    		}
	        return ReturnResult.SUCCESS(coinInfo);
		}catch(Exception e){
			logger.error("获取币种信息异常，参数shortName："+shortName,e);
		}
    	return ReturnResult.FAILUER("");
    }
    
    /**
     * 查询银行
     */
    @ResponseBody
    @RequestMapping("/v2/bank_info")
    public ReturnResult getBankInfo() 
    {
    	try{
    		List<FSystemBankinfoWithdraw> withdrawBankList = redisHelper.getWithdrawBankList();
    		return ReturnResult.SUCCESS(withdrawBankList);
		}catch(Exception e){
			logger.error("查询银行异常",e);
		}
    	return ReturnResult.FAILUER("");
    }
    
    /**
     * 获取外部行情接口
     */
    @ResponseBody
    @RequestMapping("/v2/currency/ranks")
    public ReturnResult ranks() 
    {
    	try {
    		//通过这个接口获取美元对人名币的汇率
	        String url = "http://api.coindog.com/api/v1/currency/ranks";
	        OkHttpClient client = new OkHttpClient.Builder()
	                .connectTimeout(10, TimeUnit.SECONDS)
	                .writeTimeout(10, TimeUnit.SECONDS)
	                .readTimeout(10, TimeUnit.SECONDS)
	                .build();
	        Response response = client.newCall(new Request.Builder().url(url).build()).execute();
	        JSONArray jsonObject = JSONArray.parseArray(response.body().string());
        	return ReturnResult.SUCCESS(jsonObject);
    	} catch (Exception e) {
			e.printStackTrace();
			return ReturnResult.FAILUER("");
    	}
    }
    
    /**
     * 手机下载链接
     */
    @ResponseBody
    @RequestMapping("/v2/common/dowloadUrl")
    public ReturnResult dowloadUrl() 
    {
    	try{
    		String androidUrl = redisHelper.getSystemArgs("AndroidDownloadUrl");
    		
    		String iosUrl = redisHelper.getSystemArgs("IosDownloadUrl");
            
            JSONObject result = new JSONObject();
            result.put("androidUrl", androidUrl);
            result.put("iosUrl", iosUrl);
	        return ReturnResult.SUCCESS(result);
		}catch(Exception e){
			e.printStackTrace();
		}
    	return ReturnResult.FAILUER("");
    }
    
    //获取用户id所在地，区域访问
  	@ResponseBody
  	@RequestMapping(value = "/comm/getIp")
  	public ReturnResult getIp() {
      	try {
      		HttpServletRequest request = sessionContextUtils.getContextRequest();
      		String ip = Utils.getIpAddr(request);
      		String key = "comm_ip_limit_"+ip;
      		String countStr = redisHelper.get(key);
      		if(!StringUtils.isEmpty(countStr)) {
      			return ReturnResult.SUCCESS(200,"false");
      		}
      		
      		Locator locator = Locator.loadFromLocal("/home/17monipdb.datx");
      		LocationInfo ipInfo = locator.find(ip);
  			if(ipInfo!=null && !StringUtils.isEmpty(ipInfo.country)) {
  				String country = ipInfo.country;
  				if(!StringUtils.isEmpty(country)&&country.equals("中国")) {
  					Calendar calendar = Calendar.getInstance();
  					calendar.add(Calendar.DAY_OF_YEAR, 1);
  					calendar.set(Calendar.HOUR_OF_DAY, 0);
  					calendar.set(Calendar.SECOND, 0);
  					calendar.set(Calendar.MINUTE, 0);
  					calendar.set(Calendar.MILLISECOND, 0);
  					Long timeout = ((calendar.getTimeInMillis()-System.currentTimeMillis()) / 1000);
  					
  					redisHelper.set(key, "1" ,timeout.intValue());
  					return ReturnResult.SUCCESS(200,"true");
  				}else {
  					return ReturnResult.SUCCESS(200,"false");
  				}
  			}
  		}catch (IOException e) {
  			e.printStackTrace();
  		}catch (Exception e) {
  			e.printStackTrace();
  		}
      	return ReturnResult.SUCCESS(200,"false");
  	}
  	
  	/**
     * 友情链接
     */
    @ResponseBody
    @RequestMapping("/v2/common/friendlink")
    public ReturnResult friendlink() 
    {
    	try{
    		 //友情链接
    		List<FFriendLink> ffriendlinks = redisHelper.getFFriendLinkList();
	        return ReturnResult.SUCCESS(ffriendlinks);
		}catch(Exception e){
			e.printStackTrace();
		}
    	return ReturnResult.FAILUER("");
    }
}
