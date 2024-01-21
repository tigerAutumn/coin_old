package com.qkwl.web.front.controller.c2c;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qkwl.common.Enum.validate.PlatformEnum;
import com.qkwl.common.dto.Enum.WithdrawBankTypeEnum;
import com.qkwl.common.dto.Enum.c2c.C2CBusinessTypeEnum;
import com.qkwl.common.dto.Enum.c2c.UserC2CEntrustStatusEnum;
import com.qkwl.common.dto.Enum.c2c.UserC2CEntrustTypeEnum;
import com.qkwl.common.dto.c2c.C2CBusiness;
import com.qkwl.common.dto.c2c.UserC2CEntrust;
import com.qkwl.common.dto.capital.FUserBankinfoDTO;
import com.qkwl.common.dto.capital.UserBankinfoDTO;
import com.qkwl.common.dto.common.DeviceInfo;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.user.FUserIdentity;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.c2c.C2CService;
import com.qkwl.common.rpc.capital.IUserCapitalAccountService;
import com.qkwl.common.rpc.user.IUserIdentityService;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.common.util.Utils;
import com.qkwl.web.front.controller.base.JsonBaseController;

@Controller
public class C2CController extends JsonBaseController {
	private static final Logger logger = LoggerFactory.getLogger(C2CController.class);
	
	@Autowired
	private C2CService c2cService;
	
	@Autowired
	private IUserCapitalAccountService userCapitalAccountService;
	
	@Autowired
    private RedisHelper redisHelper;
	
	@Autowired
	private IUserIdentityService userIdentityService;
	/**
     * 获取商户列表
     */
    @ResponseBody
    @RequestMapping("/c2c/businessList")
    public ReturnResult businessList(Integer coinId) 
    {
    	try{
    		//如果获取不到用户则返回报错
			FUser user = getUser();
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
	        }
			
			if(coinId == null || coinId == 0) {
				coinId = 9;
			}
			
    		List<C2CBusiness> withdraw = c2cService.selectBusinessByType(C2CBusinessTypeEnum.withdraw.getCode(),coinId);
    		List<C2CBusiness> recharge = c2cService.selectBusinessByType(C2CBusinessTypeEnum.recharge.getCode(),coinId);
    		if(withdraw==null && recharge == null ) {
    			return ReturnResult.FAILUER("");
    		}
    		JSONObject jsonObject = new JSONObject();
	        jsonObject.put("withdraw", withdraw);
	        jsonObject.put("recharge", recharge);
	        return ReturnResult.SUCCESS(jsonObject);
		}catch(Exception e){
			e.printStackTrace();
		}
    	return ReturnResult.FAILUER("");
    }
    
    @ResponseBody
    @RequestMapping("/user/getUserInfo")
    public ReturnResult userInfo() {
    	FUser user = getUser();
		if (user == null) {
            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
        }
		
		FUserIdentity identity = userIdentityService.selectByUser(user.getFid());
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("login", false);
    	if (user != null) {
            jsonObject.put("isTelephoneBind", user.getFistelephonebind());
            jsonObject.put("tradePassword", user.getFtradepassword() == null);
            jsonObject.put("needTradePasswd", redisHelper.getNeedTradePassword(user.getFid()));
            jsonObject.put("login", true);
            jsonObject.put("isRealNameVerify", user.getFhasrealvalidate());
            jsonObject.put("identity", identity);
        }
    	return ReturnResult.SUCCESS(jsonObject);
    }
    
    /**
     * 获取用户订单列表
     * @param currentPage
     * @param startTime
     * @param endTime
     * @param type
     * @param status
     * @return
     */
    @ResponseBody
    @RequestMapping("/c2c/orderList")
    public ReturnResult orderList(Integer page,String startTime,String endTime,
    		Integer type,Integer status,Integer coinId) 
    {
		try{
			//如果获取不到用户则返回报错
			FUser user = getUser();
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
	        }
			
			if(coinId == null || coinId == 0) {
				coinId = 9;
			}
			UserC2CEntrust param = new UserC2CEntrust();
			param.setStartTime(startTime);
			param.setEndTime(endTime);
			param.setType(type);
			param.setStatus(status);
			param.setUserId(user.getFid());
			param.setCoinId(coinId);
			PageInfo<UserC2CEntrust> info = c2cService.selectList(param, page, 10);
	        return ReturnResult.SUCCESS(info);
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		return ReturnResult.FAILUER("");
    }
    
    
    /**
     * 生成充值提现订单
     * @return
     */
    @ResponseBody
    @RequestMapping("/c2c/order")
    public ReturnResult createOrder(BigDecimal amount,Integer type,
    		Integer businessId,Integer coinId) 
    {
		try{
			String ip = Utils.getIpAddr(sessionContextUtils.getContextRequest());
			
			//如果获取不到用户则返回报错
			FUser user = getUser();
			if(user == null) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
			}
			
			if(coinId == null || coinId == 0) {
				coinId = 9;
			}
			
			String key = "c2c_cancel_limit_" + user.getFid();
			
			String countStr = redisHelper.get(key);
	        if(StringUtils.isNotEmpty(countStr)) {
	        	int count = Integer.parseInt(countStr);
	            if(count >= 3) {
	            	return ReturnResult.FAILUER("您取消的订单数已达到3笔，被限制进行C2C交易，请30分钟后再进行操作！");
	            }
	        }
			
			DeviceInfo deviceInfo = getDeviceInfo();
			int platform = deviceInfo.getPlatform();
			
			UserC2CEntrust param = new UserC2CEntrust();
			param.setUserId(user.getFid());
			param.setType(type);
			param.setAmount(amount);
			param.setBusinessId(businessId);
			param.setIp(ip);
			param.setCoinId(coinId);
			param.setPlatform(platform);
			
			//生成订单
			Result result = c2cService.createEntrust(param);
			if (result.getSuccess()) {
                return ReturnResult.SUCCESS(result.getData());
            } else if (result.getCode().equals(Result.PARAM)) {
                logger.error("createOrder is param error, {}", result.getMsg());
            } else if (result.getCode() >= 10000) {
                return ReturnResult.FAILUER(result.getCode(), super.GetR18nMsg("com.error." + result.getCode().toString(), result.getData().toString()));
            } else {
                return ReturnResult.FAILUER(result.getCode(), super.GetR18nMsg("c2c.entrust.error." + result.getCode().toString(), result.getData().toString()));
            }
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return ReturnResult.FAILUER("");
    }
    
    /**
     * 订单详情
     * @return
     */
    @ResponseBody
    @RequestMapping("/c2c/orderDetail")
    public ReturnResult orderDetail(Integer orderId) 
    {
		try{
			if(orderId == null ) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
			}
			
			//如果获取不到用户则返回报错
			FUser user = getUser();
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
	        }
			
			//查询对应的订单，如果不存在则报错
			UserC2CEntrust userC2CEntrust = c2cService.selectOrderById(orderId);
			if(userC2CEntrust == null) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
			}
			
			String bankAccount = userC2CEntrust.getBankAccount(); 
			String bankCode = userC2CEntrust.getBankCode();
			String phone = userC2CEntrust.getPhone();
			String bank = userC2CEntrust.getBank();
			if(userC2CEntrust.getType().equals(UserC2CEntrustTypeEnum.withdraw.getCode())) {
				if(StringUtils.isNotEmpty(bankAccount)) {
					userC2CEntrust.setBankAccount("*******");
				}
				
				if(StringUtils.isNotEmpty(bankCode)) {
					userC2CEntrust.setBankCode("**************");
				}
				
				if(StringUtils.isNotEmpty(phone)) {
					userC2CEntrust.setPhone("*******");
				}
				
				if(StringUtils.isNotEmpty(bank)) {
					userC2CEntrust.setBank(Utils.replaceAction(bank));
				}
			}
			
	        return ReturnResult.SUCCESS(userC2CEntrust);
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		return ReturnResult.FAILUER("");
    }
    
    /**
     * 系统参数配置
     * @return
     */
    @ResponseBody
    @RequestMapping("/c2c/setting")
    public ReturnResult setting() 
    {
		try{
			//如果获取不到用户则返回报错
			FUser user = getUser();
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
	        }
			
			Map<String, String> param = c2cService.getParam();
			JSONObject obj = new JSONObject();
			obj.put("minBuyAmount", param.get("minBuyAmount"));
			obj.put("buyPrice", param.get("buyPrice"));
			obj.put("sellPrice", param.get("sellPrice"));
			obj.put("buyExpireTime", param.get("buyExpireTime"));
			obj.put("minSellAmount", param.get("minSellAmount"));
			obj.put("digit", param.get("digit"));
			
			obj.put("isOpenGSET", param.get("isOpenGSET"));
			obj.put("isOpenUSDT", param.get("isOpenUSDT"));
			
			//获取usdt最新价格
			String usdtSellPrice = param.get("usdtSellPrice");
			String usdtBuyPrice = param.get("usdtBuyPrice");
			String usdtPriceStr = getCNYValue();
			BigDecimal usdtPrice = new BigDecimal(usdtPriceStr);
			BigDecimal addPrice = new BigDecimal(usdtBuyPrice);
			BigDecimal subPrice = new BigDecimal(usdtSellPrice);
			obj.put("usdtSellPrice", MathUtils.sub(usdtPrice, subPrice));
			obj.put("usdtBuyPrice", MathUtils.add(usdtPrice, addPrice));
			
			UserC2CEntrust record = new UserC2CEntrust();
			record.setUserId(user.getFid());
			List<Integer> statusList = new ArrayList<>();
			statusList.add(UserC2CEntrustStatusEnum.processing.getCode());
			statusList.add(UserC2CEntrustStatusEnum.wait.getCode());
			record.setStatusList(statusList);
			int count = c2cService.getEntrustCount(record);
			obj.put("count", count);
	        return ReturnResult.SUCCESS(obj);
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		return ReturnResult.FAILUER("");
    }
    
    /**
     * 用户绑定银行卡列表
     * @return
     */
    @ResponseBody
    @RequestMapping("/c2c/userBanklist")
    public ReturnResult userBanklist() 
    {
		try{
			//如果获取不到用户则返回报错
			FUser user = getUser();
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
	        }
			
			//查询对应的订单，如果不存在则报错
			List<FUserBankinfoDTO> list = userCapitalAccountService.listBankInfo(user.getFid(), null,WithdrawBankTypeEnum.Bank.getCode());
			if(list == null) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
			}
			
	        return ReturnResult.SUCCESS(list);
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		return ReturnResult.FAILUER("");
    }
    
    /**
	 * 新增银行卡
	 */
	@ResponseBody
	@RequestMapping(value = "/c2c/save_bankinfo")
	public ReturnResult updateOutAddress(
			@RequestParam(required = true) String account,
			@RequestParam(required = true) String address,
			@RequestParam(required = true) String prov,
			@RequestParam(required = true) String city,
			@RequestParam(required = false) String dist,
			@RequestParam(required = true) String realName,
			@RequestParam(required = true) Integer bankId,
			@RequestParam(required = true) String password) throws Exception {
		try{
			String ip = getIpAddr();
			// 用户
			FUser fuser = getUser();

			UserBankinfoDTO userBankinfo = new UserBankinfoDTO();
			userBankinfo.setUserId(fuser.getFid());
			userBankinfo.setSystemBankId(bankId);
			userBankinfo.setRealName(realName);
			userBankinfo.setBankNumber(HtmlUtils.htmlEscape(account));
			userBankinfo.setProv(prov);
			userBankinfo.setCity(city);
			userBankinfo.setDist(dist);
			userBankinfo.setAddress(address);
			userBankinfo.setPlatform(PlatformEnum.BC);
			userBankinfo.setPassword(Utils.MD5(password));
			userBankinfo.setType(WithdrawBankTypeEnum.Bank.getCode());
			userBankinfo.setIp(ip);

			//新增接口。去掉谷歌验证码和手机验证码，但是加上了交易密码验证
			Result result = userCapitalAccountService.createOrUpdateBankInfoV1(userBankinfo);
			if(result.getCode() == 200){
				return ReturnResult.SUCCESS();
			} else if(result.getCode() > 200 && result.getCode() < 1000){
				return ReturnResult.FAILUER(GetR18nMsg("common.error." + result.getCode()));
			} else if(result.getCode() >= 1000 && result.getCode() < 10000){
				if(result.getCode().equals(1003)) {
					 return ReturnResult.FAILUER(result.getCode(), super.GetR18nMsg("c2c.entrust.error." + result.getCode().toString(), result.getData().toString()));
				}
				return ReturnResult.FAILUER(GetR18nMsg("capital.bank.withdraw." + result.getCode()));
			} else{
				return ReturnResult.FAILUER(GetR18nMsg("com.error." + result.getCode(), result.getData()));
			}
		}catch (Exception e){
			logger.error("新增提现银行卡异常：", e);
			return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
		}
	}
	
	
	/**
	 * 修改银行卡
	 */
	@ResponseBody
	@RequestMapping(value = "/c2c/update_bankinfo")
	public ReturnResult updateBankinfo(
			@RequestParam(required = true) Integer id,
			@RequestParam(required = true) String account,
			@RequestParam(required = true) String address,
			@RequestParam(required = true) String prov,
			@RequestParam(required = true) String city,
			@RequestParam(required = false) String dist,
			@RequestParam(required = true) String realName,
			@RequestParam(required = true) Integer bankId,
			@RequestParam(required = true) String password) throws Exception {
		try{
			String ip = getIpAddr();
			// 用户
			FUser fuser = getUser();

			UserBankinfoDTO userBankinfo = new UserBankinfoDTO();
			userBankinfo.setUserId(fuser.getFid());
			userBankinfo.setSystemBankId(bankId);
			userBankinfo.setRealName(realName);
			userBankinfo.setBankNumber(HtmlUtils.htmlEscape(account));
			userBankinfo.setProv(prov);
			userBankinfo.setCity(city);
			userBankinfo.setDist(dist);
			userBankinfo.setAddress(address);
			userBankinfo.setPlatform(PlatformEnum.BC);
			userBankinfo.setPassword(Utils.MD5(password));
			userBankinfo.setType(WithdrawBankTypeEnum.Bank.getCode());
			userBankinfo.setIp(ip);
			userBankinfo.setId(id);

			//新增接口。去掉谷歌验证码和手机验证码，但是加上了交易密码验证
			Result result = userCapitalAccountService.createOrUpdateBankInfoV1(userBankinfo);
			if(result.getCode() == 200){
				return ReturnResult.SUCCESS();
			} else if(result.getCode() > 200 && result.getCode() < 1000){
				return ReturnResult.FAILUER(GetR18nMsg("common.error." + result.getCode()));
			} else if(result.getCode() >= 1000 && result.getCode() < 10000){
				if(result.getCode().equals(1003)) {
					 return ReturnResult.FAILUER(result.getCode(), super.GetR18nMsg("c2c.entrust.error." + result.getCode().toString(), result.getData().toString()));
				}
				return ReturnResult.FAILUER(GetR18nMsg("capital.bank.withdraw." + result.getCode()));
			} else{
				return ReturnResult.FAILUER(GetR18nMsg("com.error." + result.getCode(), result.getData()));
			}
		}catch (Exception e){
			logger.error("新增提现银行卡异常：", e);
			return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
		}
	}
	
	/**
	 * 删除银行卡银行卡
	 */
	@ResponseBody
	@RequestMapping(value = "/c2c/del_bankinfo")
	public ReturnResult updateOutAddress(Integer bankId) {
		//如果获取不到用户则返回报错
		FUser user = getUser();
		if (user == null) {
            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
        }
		
		Result result = userCapitalAccountService.deleteBankInfo(user.getFid(), bankId);
		
		if(result.getCode() == 200){
			return ReturnResult.SUCCESS();
		} else if(result.getCode() > 200 && result.getCode() < 1000){
			return ReturnResult.FAILUER(GetR18nMsg("common.error." + result.getCode()));
		} else if(result.getCode() >= 1000 && result.getCode() < 10000){
			return ReturnResult.FAILUER(GetR18nMsg("capital.bank.withdraw." + result.getCode()));
		} else{
			return ReturnResult.FAILUER(GetR18nMsg("com.error." + result.getCode(), result.getData()));
		}
	}
	
	
	/**
	 * 设置默认银行卡
	 */
	@ResponseBody
	@RequestMapping(value = "/c2c/default_bankinfo")
	public ReturnResult default_bankinfo(Integer bankId) {
		//如果获取不到用户则返回报错
		FUser user = getUser();
		if (user == null) {
            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
        }
		
		Result result = userCapitalAccountService.defaultBankInfo(user.getFid(), bankId);
		
		if(result.getCode() == 200){
			return ReturnResult.SUCCESS();
		} else if(result.getCode() > 200 && result.getCode() < 1000){
			return ReturnResult.FAILUER(GetR18nMsg("common.error." + result.getCode()));
		} else if(result.getCode() >= 1000 && result.getCode() < 10000){
			return ReturnResult.FAILUER(GetR18nMsg("capital.bank.withdraw." + result.getCode()));
		} else{
			return ReturnResult.FAILUER(GetR18nMsg("com.error." + result.getCode(), result.getData()));
		}
	}
	
	
	/**
	 * 修改订单状态
	 */
	@ResponseBody
	@RequestMapping(value = "/c2c/updateOrder")
	public ReturnResult updateOrder(Integer orderId) {
		//如果获取不到用户则返回报错
		FUser user = getUser();
		if (user == null) {
            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
        }
		
		boolean result = c2cService.updateEntrust(orderId);
		if(result) {
			return ReturnResult.SUCCESS();
		}else {
			return ReturnResult.FAILUER("确认失败，当前订单已经在处理中了！");
		}
	}
	
	
	/**
	 * 取消订单
	 */
	@ResponseBody
	@RequestMapping(value = "/c2c/cancelOrder")
	public ReturnResult cancelOrder(Integer orderId) {
		//如果获取不到用户则返回报错
		FUser user = getUser();
		if (user == null) {
            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
        }
		
		String key = "c2c_cancel_limit_" + user.getFid();
		
		boolean result = c2cService.cancelEntrust(orderId);
		if(result) {
			//撤销过三次订单则需要提示用户不准撤销订单了。
			redisHelper.getIncrByKey(key,1800);
			return ReturnResult.SUCCESS();
		}else {
			return ReturnResult.FAILUER("撤单失败，当前订单已经在处理中了！");
		}
	}
}
