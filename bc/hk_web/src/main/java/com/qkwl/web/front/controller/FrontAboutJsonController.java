package com.qkwl.web.front.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.dto.Enum.SystemCoinStatusEnum;
import com.qkwl.common.dto.Enum.SystemCoinTypeEnum;
import com.qkwl.common.dto.Enum.VirtualCapitalOperationTypeEnum;
import com.qkwl.common.dto.app.VersionUpgradeInfo;
import com.qkwl.common.dto.capital.FUserVirtualAddressWithdrawDTO;
import com.qkwl.common.dto.capital.FVirtualCapitalOperationDTO;
import com.qkwl.common.dto.coin.SystemCoinSetting;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.coin.SystemCoinTypeVO;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.user.FUserDTO;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.dto.web.FAbout;
import com.qkwl.common.dto.web.FAboutType;
import com.qkwl.common.dto.web.FSystemLan;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.i18n.LuangeHelper;
import com.qkwl.common.redis.RedisConstant;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.ModelMapperUtils;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.web.front.controller.base.JsonBaseController;
import com.qkwl.web.utils.WebConstant;

@Controller
public class FrontAboutJsonController extends JsonBaseController {

    @Autowired
    private RedisHelper redisHelper;

    /**
     * 关于我们
     */
    @ResponseBody
    @RequestMapping(value = "/about/about_json")
    public ReturnResult index(@RequestParam(required = false, defaultValue = "0") Integer id) throws Exception {
        //ModelAndView modelAndView = new ModelAndView();
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
        if (systemLan == null) {
            return ReturnResult.FAILUER("");
        }
        // 获取当前语言下所有的about种类
        List<FAboutType> fabouttypes = redisHelper.getAboutTypeList(1, WebConstant.BCAgentId);
        // 查找当前语言下制定id下的详细信息
        FAbout fabout = redisHelper.getAbout(id, 1, WebConstant.BCAgentId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fabout", fabout);
        jsonObject.put("fabouttypes", fabouttypes);
        return ReturnResult.SUCCESS(jsonObject);
    }

    /**
     * 关于我们
     * @param type 1：安卓 2：ios
     */
    @ResponseBody
    @RequestMapping(value = "/about/app_version_json")
    public ReturnResult appVersionInfo(@RequestParam(required = false, defaultValue = "1") Integer type) throws Exception {
        VersionUpgradeInfo appVersion = redisHelper.getAppVersion(type);
        return ReturnResult.SUCCESS(appVersion);
    }
    
    /**
     * 查询各个虚拟币提现手续费
     */
    @ResponseBody
    @RequestMapping("/about/withdraw_fee_json")
    public ReturnResult withdrawfFee() {
    	FUser user = getCurrentUserInfoByToken();
    	if(user == null) {
    		user = new FUser();
    		user.setLevel(0);
    	}
        // 币种查找
    	List<SystemCoinType> coinTypeList = redisHelper.getCoinTypeList();
    	if(coinTypeList == null) {
    		return ReturnResult.FAILUER("");
    	}
    	JSONObject jsonObject = new JSONObject();
    	JSONArray jsonArray = new JSONArray();
    	for (SystemCoinType systemCoinType : coinTypeList) {
    		JSONObject jo= new JSONObject();
    		SystemCoinSetting coinSetting = redisHelper.getCoinSetting(systemCoinType.getId(), user.getLevel());
    		if(coinSetting == null) {
    			continue;
    		}
    		jo.put("logo", systemCoinType.getWebLogo());
    		jo.put("shortName", systemCoinType.getShortName());
    		jo.put("name", systemCoinType.getName());
    		jo.put("withdrawMin", coinSetting.getWithdrawMin());
    		jo.put("isPercentage", coinSetting.getIsPercentage());
    		jo.put("withdrawFee", coinSetting.getWithdrawFee());
    		jsonArray.add(jo);
		}
    	//币种查询手续费
    	jsonObject.put("coinList", jsonArray);
    	String systemArgs = redisHelper.getSystemArgs(RedisConstant.TRADE_FEE);
    	if(!StringUtils.isEmpty(systemArgs)) {
    		jsonObject.put("tradeFee", systemArgs);
    	}else{
    		jsonObject.put("tradeFee", "0.002");
    	}
    	jsonObject.put("rechargeFee", "免费");
        return ReturnResult.SUCCESS(jsonObject);
    }
    
    /**
     * 币种列表
     */
    @ResponseBody
    @RequestMapping("/about/virtual_coin_json")
    public ReturnResult coinList() {
    	List<SystemCoinTypeVO> mapper = ModelMapperUtils.mapper(
                redisHelper.getCoinTypeCoinList(), SystemCoinTypeVO.class);
        return ReturnResult.SUCCESS(mapper);
    }

}
