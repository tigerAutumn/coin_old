package com.qkwl.web.front.controller.base;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.qkwl.common.Enum.validate.LocaleEnum;
import com.qkwl.common.Enum.validate.PlatformEnum;
import com.qkwl.common.controller.BaseController;
import com.qkwl.common.dto.Enum.UserLoginType;
import com.qkwl.common.dto.market.TickerData;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.user.LoginResponse;
import com.qkwl.common.dto.user.RequestUserInfo;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.push.PushService;
import com.qkwl.common.rpc.user.IUserService;
import com.qkwl.common.util.Utils;
import com.qkwl.web.utils.WebConstant;

public class RedisBaseControll extends BaseController {

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private PushService pushService;
    
/*    @Autowired
    private IUserService userService;*/

    public FUser getCurrentUserInfoByToken() {
/*			try {
		    	RequestUserInfo requestUserInfo = new RequestUserInfo();
				requestUserInfo.setFloginname("18062789252");
				requestUserInfo.setType(0);
				requestUserInfo.setFagentid(WebConstant.BCAgentId);
				requestUserInfo.setPlatform(PlatformEnum.BC);
				requestUserInfo.setFloginpassword(Utils.MD5("1234abcd"));
				Result result = userService.updateCheckLogin(requestUserInfo, UserLoginType.WebUser, "192.168.0.193", LocaleEnum.ZH_TW);
				return (FUser) ((LoginResponse)result.getData()).getUserinfo();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			
        String webToken = sessionContextUtils.getContextToken("token");
        if(StringUtils.isNotEmpty(webToken)) {
        	return redisHelper.getCurrentUserInfoByToken(webToken);
        }
        
        String appToken = sessionContextUtils.getContextApiToken();
        if(StringUtils.isNotEmpty(appToken)) {
        	return redisHelper.getCurrentUserInfoByToken(appToken);
        }
		return null;
    }

    public FUser getCurrentUserInfoByApiToken(){
        String token = sessionContextUtils.getContextApiToken();
        return redisHelper.getCurrentUserInfoByToken(token);
    }

    public BigDecimal getLastPrice(int coinid) {
        TickerData tickerData = pushService.getTickerData(coinid);
        return tickerData.getLast();
    }

    public void deleteUserInfo() {
        String token = sessionContextUtils.getContextToken("token");
        redisHelper.deleteUserInfo(token);
    }
    
    public void deleteApiUserInfo() {
    	String token = sessionContextUtils.getContextApiToken();
        redisHelper.deleteUserInfo(token);
    }

    public void updateUserInfo(FUser user) {
        String webToken = sessionContextUtils.getContextToken("token");
        if(StringUtils.isNotEmpty(webToken)) {
        	redisHelper.updateUserInfo(webToken, user);
        }
        
        String appToken = sessionContextUtils.getContextApiToken();
        if(StringUtils.isNotEmpty(appToken)) {
        	redisHelper.updateUserInfo(appToken, user);
        }
    }
    
    public void updateApiUserInfo(FUser user) {
    	String token = sessionContextUtils.getContextApiToken();
        redisHelper.updateUserInfo(token, user);
    }

    public String setRedisData(String token, Object restInfo) {
        String redisKey = Utils.UUID();
        sessionContextUtils.addContextToken(token, redisKey);
        redisHelper.setRedisData(redisKey, restInfo);
        return redisKey;
    }

    public void deletRedisData(String token) {
        String redisKey = sessionContextUtils.getContextToken(token);
        redisHelper.deletRedisData(redisKey);
    }

    public String getRedisData(String token) {
        String redisKey = sessionContextUtils.getContextToken(token);
        return redisHelper.getRedisData(redisKey);
    }
}
