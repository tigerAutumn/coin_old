package com.qkwl.web.front.controller.base;

import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.Enum.validate.LocaleEnum;
import com.qkwl.common.dto.common.DeviceInfo;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.i18n.LuangeHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.util.Utils;
import com.qkwl.web.utils.WebConstant;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

public class JsonBaseController extends RedisBaseControll {

    @Autowired 
    private RedisHelper redisHelper;

    /**
     * 获取多语言
     *
     * @param key 键值
     * @return
     */
    public String GetR18nMsg(String key) {
        return LuangeHelper.GetR18nMsg(sessionContextUtils.getContextRequest(), key);
    }

    public FUser getUser() {
    	FUser webUser = getCurrentUserInfoByToken();
    	if(webUser != null) {
    		return webUser;
    	}
    	
    	FUser appUser = getCurrentUserInfoByApiToken();
    	if(appUser != null) {
    		return appUser;
    	}
    	return null;
    }
    
    /**
     * 获取多语言
     *
     * @param key  键值
     * @param args 参数
     * @return
     */
    public String GetR18nMsg(String key, Object... args) {
        return LuangeHelper.GetR18nMsg(sessionContextUtils.getContextRequest(), key, args);
    }

    /**
     * 获取语言枚举
     *
     * @return
     */
    public LocaleEnum getLanEnum() {
        String localeStr = LuangeHelper.getLan(sessionContextUtils.getContextRequest());
        for (LocaleEnum locale : LocaleEnum.values()) {
            if (locale.getName().equals(localeStr)) {
                return locale;
            }
        }
        return null;
    }

    public String getIpAddr() {
        return Utils.getIpAddr(sessionContextUtils.getContextRequest());
    }

    public String getLan() {
        return LuangeHelper.getLan(sessionContextUtils.getContextRequest());
    }

    /**
     * 净资产
     */
    public BigDecimal getNetAssets(List<UserCoinWallet> coinWallets) {
        Map<Integer, Integer> trades = redisHelper.getCoinIdToTradeId(WebConstant.BCAgentId);
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal assets, price;
        Integer tradeId;
        for (UserCoinWallet coinWallet : coinWallets) {
            // 人民币
            if (coinWallet.getCoinId().equals(9)) {
                assets = MathUtils.add(coinWallet.getTotal(), coinWallet.getFrozen());
                totalAssets = MathUtils.add(totalAssets, assets);
                continue;
            }
            // 虚拟币
            tradeId = trades.get(coinWallet.getCoinId());
            if (tradeId == null) {
                continue;
            }
            price = redisHelper.getLastPrice(tradeId);
            assets = MathUtils.add(coinWallet.getTotal(), coinWallet.getFrozen());
            assets = MathUtils.mul(assets, price);
            totalAssets = MathUtils.add(totalAssets, assets);
        }
        return MathUtils.toScaleNum(totalAssets, MathUtils.DEF_CNY_SCALE);
    }

    /**
     * 总资产
     */
    public BigDecimal getTotalAssets(List<UserCoinWallet> coinWallets) {
        Map<Integer, Integer> trades = redisHelper.getCoinIdToTradeId(WebConstant.BCAgentId);
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal assets, price;
        Integer tradeId;
        for (UserCoinWallet coinWallet : coinWallets) {
            // 人民币
            if (coinWallet.getCoinId().equals(9)) {
                assets = MathUtils.add(coinWallet.getTotal(), coinWallet.getFrozen());
                assets = MathUtils.add(assets, coinWallet.getBorrow());
                totalAssets = MathUtils.add(totalAssets, assets);
                continue;
            }
            // 虚拟币
            tradeId = trades.get(coinWallet.getCoinId());
            if (tradeId == null) {
                continue;
            }
            price = redisHelper.getLastPrice(tradeId);
            assets = MathUtils.add(coinWallet.getTotal(), coinWallet.getFrozen());
            assets = MathUtils.add(assets, coinWallet.getBorrow());
            assets = MathUtils.mul(assets, price);
            totalAssets = MathUtils.add(totalAssets, assets);
        }
        
        return MathUtils.toScaleNum(totalAssets, MathUtils.DEF_CNY_SCALE);
    }
    
    //获取公共请求参数
    public DeviceInfo getDeviceInfo() {
    	HttpServletRequest requset = sessionContextUtils.getContextRequest();
    	String platform = requset.getHeader("platform");
    	String deviceId = requset.getHeader("deviceId");
    	String versionCode = requset.getHeader("versionCode");
    	String buildCode = requset.getHeader("buildCode");
    	String sysVer = requset.getHeader("sysVer");
    	String deviceModel = requset.getHeader("deviceModel");
    	DeviceInfo deviceInfo = new DeviceInfo();
    	//如果请求头获取的为空则从参数中获取
    	if(StringUtils.isEmpty(platform)) {
    		//从参数中获取
    		deviceInfo.setBuildCode(requset.getParameter("buildCode"));
        	deviceInfo.setPlatform(Integer.parseInt(requset.getParameter("platform")));
        	deviceInfo.setDeviceId(requset.getParameter("deviceId"));
        	deviceInfo.setSysVer(requset.getParameter("sysVer"));
        	deviceInfo.setDeviceModel(requset.getParameter("deviceModel"));
        	deviceInfo.setVersionCode(requset.getParameter("versionCode"));
    	}else{
    		//从请求头获取
    		deviceInfo.setBuildCode(buildCode);
        	deviceInfo.setPlatform(Integer.parseInt(platform));
        	deviceInfo.setDeviceId(deviceId);
        	deviceInfo.setSysVer(sysVer);
        	deviceInfo.setDeviceModel(deviceModel);
        	deviceInfo.setVersionCode(versionCode);
    	}
		return deviceInfo;
    }	
    
    

    public String getCNYValue() {
    	try {
	        String cny_value = redisHelper.getRedisData("CNY_VALUE");
	        if (!TextUtils.isEmpty(cny_value)) {
	            return cny_value;
	        }
	      //通过这个接口获取美元对人名币的汇率
	        String url = "http://data.fixer.io/api/latest?access_key=bf18da33f4011c6dc52c839a4688bd3b";
	        OkHttpClient client = new OkHttpClient.Builder()
	                .connectTimeout(10, TimeUnit.SECONDS)
	                .writeTimeout(10, TimeUnit.SECONDS)
	                .readTimeout(10, TimeUnit.SECONDS)
	                .build();
	        
	        Response response;
			
				response = client.newCall(new Request.Builder()
				        .url(url)
				        .build())
				        .execute();
			
	        JSONObject jsonObject = JSONObject.parseObject(response.body().string());
	        JSONObject rates = jsonObject.getJSONObject("rates");
	        JSONObject resultJson = new JSONObject();
	        BigDecimal bigDecimal = rates.getBigDecimal("CNY");
	        BigDecimal bigDecimal2 = rates.getBigDecimal("USD");
	        BigDecimal bg = bigDecimal.divide(bigDecimal2,bigDecimal.ROUND_HALF_UP);
	        cny_value = bg.toString();
	        setCNYValue(bg.toString());
        	return cny_value;
    	} catch (IOException e) {
			return null;
    	}
    }

    public void setCNYValue(String cny) {
        if (TextUtils.isEmpty(cny)) {
            return;
        }
        redisHelper.setRedisData("CNY_VALUE", cny, 60 * 60);
    }

}
