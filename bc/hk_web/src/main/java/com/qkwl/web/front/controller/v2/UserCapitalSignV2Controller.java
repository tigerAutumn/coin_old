package com.qkwl.web.front.controller.v2;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.Enum.validate.PlatformEnum;
import com.qkwl.common.dto.Enum.DataSourceEnum;
import com.qkwl.common.dto.Enum.LimitTypeEnum;
import com.qkwl.common.dto.Enum.SystemCoinSortEnum;
import com.qkwl.common.dto.Enum.SystemCoinStatusEnum;
import com.qkwl.common.dto.Enum.SystemCoinTypeEnum;
import com.qkwl.common.dto.Enum.UserStatusEnum;
import com.qkwl.common.dto.Enum.VirtualCapitalOperationTypeEnum;
import com.qkwl.common.dto.capital.CoinOperationOrderDTO;
import com.qkwl.common.dto.capital.FUserVirtualAddressWithdrawDTO;
import com.qkwl.common.dto.capital.UserVirtualAddressWithdrawDTO;
import com.qkwl.common.dto.coin.SystemCoinSetting;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.framework.validate.ValidationCheckHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.capital.IUserCapitalAccountService;
import com.qkwl.common.rpc.capital.IUserCapitalService;
import com.qkwl.common.rpc.capital.IUserWalletService;
import com.qkwl.common.rpc.user.IUserService;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.common.util.Utils;
import com.qkwl.web.front.controller.base.JsonBaseController;

@Controller
public class UserCapitalSignV2Controller extends JsonBaseController {
	private static final Logger logger = LoggerFactory.getLogger(UserCapitalSignV2Controller.class);
	@Autowired
	private IUserService userService;
	@Autowired
	private RedisHelper redisHelper;
	
	@Autowired
	private IUserCapitalAccountService userCapitalAccountService;
	
    @Autowired
    private IUserWalletService userWalletService;
    
    @Autowired
    private IUserCapitalService userCapitalService;
    
    @Autowired
    private ValidationCheckHelper validationCheckHelper;

	/**
	 * 新增提现地址
	 * @param coinId  币种id
	 * @param withdrawAddr  提现地址
	 * @param password  交易密码
	 * @param remark  备注
	 * @param memo  地址标签
	 * @return Result   
	 */
	@ResponseBody
    @RequestMapping("/v2/capital/save_withdraw_address_sign")
	public ReturnResult addWithdrawAddress(
			@RequestParam(required = true) int coinId,
			@RequestParam(required = true) String withdrawAddr,
			@RequestParam(required = true) String password,
			@RequestParam(required = true) String remark,
			@RequestParam(required = false) String memo
			) throws Exception {
		try{
			FUser fuser = getUser();
			fuser = userService.selectUserById(fuser.getFid());
			String ip = getIpAddr();
			//查找币种
			SystemCoinType coinType = redisHelper.getCoinType(coinId);
			if (coinType == null || coinType.getStatus().equals(SystemCoinStatusEnum.ABNORMAL.getCode())) {
				return ReturnResult.FAILUER(GetR18nMsg("com.trade.error.10000"));
			}
			// 地址判断
			if(coinType.getCoinType().equals(SystemCoinSortEnum.GXS.getCode())
				|| 	coinType.getCoinType().equals(SystemCoinSortEnum.EOS.getCode())) {
				//公信宝及eos地址不做处理
				
			}else if(coinType.getCoinType().equals(SystemCoinSortEnum.ETH.getCode()) || coinType.getCoinType().equals(SystemCoinSortEnum.MOAC.getCode())){
				if(!withdrawAddr.startsWith("0x")) {
					return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11017"));
				}
			}else if(coinType.getCoinType().equals(SystemCoinSortEnum.FOD.getCode()) ) {
				//fod地址40位
				if( withdrawAddr.length() != 40) {
					return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11017"));
				}
			}
			UserVirtualAddressWithdrawDTO address = new UserVirtualAddressWithdrawDTO();
			address.setFuid(fuser.getFid());
			address.setInit(true);
			address.setFcreatetime(new Date());
			address.setFadderess(withdrawAddr);
			address.setFcoinid(coinType.getId());
			address.setFremark(remark);
			address.setVersion(0);
			address.setIp(ip);
			address.setPassword(Utils.MD5(password));
			address.setPlatform(PlatformEnum.BC);
			address.setMemo(memo);
			Result result = userCapitalAccountService.createCoinAddressWithdraw(address);
			if(result.getCode() == 200){
				return ReturnResult.SUCCESS();
			} else if(result.getCode() > 200 && result.getCode() < 1000){
				return ReturnResult.FAILUER(GetR18nMsg("common.error" + result.getCode()));
			} else if(result.getCode() >= 1000 && result.getCode() < 10000){
				return ReturnResult.FAILUER(GetR18nMsg("user.address.error." + result.getCode()));
			} else{
				return ReturnResult.FAILUER(GetR18nMsg("com.error."+result.getCode(), result.getData().toString()));
			}
		}catch (Exception e){
			logger.error("添加提现地址异常", e);
			return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
		}
	}
    
    /**
     * 虚拟币提现申请
     * @param addressId   地址id
     * @param withdrawAmount   提币金额
     * @param tradePwd   提现密码
     * @param coinId   币种id
     * @param googleCode   谷歌验证码
     * @param phoneCode   手机验证码
     * @param emailCode   邮箱验证码
     * @param btcfeesIndex   btc手续费选择
     * @param memo   地址标签
     * 
     */
    @ResponseBody
    @RequestMapping(value = "/v2/capital/withdraw_sign")
    public ReturnResult withdrawBtcSubmit(
            @RequestParam Integer addressId,
            @RequestParam BigDecimal withdrawAmount,
            @RequestParam String tradePwd,
            @RequestParam Integer coinId,
            @RequestParam(required = false, defaultValue = "0") String googleCode,
            @RequestParam(required = false, defaultValue = "0") String phoneCode,
            @RequestParam(required = false, defaultValue = "0") String emailCode,
            @RequestParam(required = false, defaultValue = "0") Integer btcfeesIndex,
            @RequestParam(required = false) String memo) {
        if (addressId == null) {
            return ReturnResult.FAILUER(GetR18nMsg("capital.coin.withdraw.com.1000"));
        }
        if (withdrawAmount == null) {
            return ReturnResult.FAILUER(GetR18nMsg("capital.coin.withdraw.com.1001"));
        }
        withdrawAmount = MathUtils.toScaleNum(withdrawAmount, MathUtils.ENTER_COIN_SCALE);
        if (withdrawAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return ReturnResult.FAILUER(GetR18nMsg("capital.coin.withdraw.com.1002"));
        }
        if (StringUtils.isEmpty(tradePwd)) {
            return ReturnResult.FAILUER(GetR18nMsg("com.error.10116"));
        }
        // 用户
        FUser user = getUser();
        if(!user.getFhasrealvalidate()) {
        	return ReturnResult.FAILUER(GetR18nMsg("com.user.security.1011"));
        }
        // 币信息
        SystemCoinType coinType = redisHelper.getCoinType(coinId);
        if (coinType == null || !coinType.getIsWithdraw() || coinType.getStatus().equals(SystemCoinStatusEnum.ABNORMAL.getCode())) {
            return ReturnResult.FAILUER(GetR18nMsg("com.error.10117"));
        }
        String ip = Utils.getIpAddr(sessionContextUtils.getContextRequest());
        // BTC网络手续费
        BigDecimal BTCFees = coinType.getNetworkFee();
        if (coinType.getShortName().equals("BTC")) {
            if (btcfeesIndex <= 0 || btcfeesIndex >= Constant.BTC_FEES_MAX) {
                BTCFees = Constant.BTC_FEES[0];
            } else {
                BTCFees = Constant.BTC_FEES[btcfeesIndex];
            }
        }
        // 提现
        try {
            tradePwd = Utils.MD5(tradePwd);
        } catch (Exception e) {
            return ReturnResult.FAILUER(GetR18nMsg("com.error.10117"));
        }
        if (coinType.getShortName().equals("BTK") && MathUtils.toScaleNum(withdrawAmount, MathUtils.INTEGER_SCALE).compareTo(withdrawAmount) != 0) {
            return ReturnResult.FAILUER(GetR18nMsg("com.error.10119"));
        }
        Boolean risk = coinType.getRiskNum() != null && coinType.getRiskNum().compareTo(BigDecimal.ZERO) > 0 && coinType.getRiskNum().compareTo(withdrawAmount) < 0;
        CoinOperationOrderDTO order = new CoinOperationOrderDTO();
        order.setUserId(user.getFid());
        order.setCoinId(coinType.getId());
        order.setCoinName(coinType.getName());
        order.setOperationType(VirtualCapitalOperationTypeEnum.COIN_OUT);
        order.setDataSource(DataSourceEnum.WEB);
        order.setPlatform(PlatformEnum.BC);
        order.setAddressBindId(addressId);
        order.setAmount(withdrawAmount);
        order.setTradePass(tradePwd);
        order.setPhoneCode(phoneCode);
        order.setGoogleCode(googleCode);
        order.setEmailCode(emailCode);
        order.setIp(ip);
        order.setRisk(risk);
        order.setNetworkFees(BTCFees);
        order.setMemo(StringUtils.isEmpty(memo) ? null : memo);
        try {
            Result result = userCapitalService.createCoinOperation(order);
            if (result.getSuccess()) {
                return ReturnResult.SUCCESS(GetR18nMsg("capital.coin.withdraw.create." + result.getCode().toString()));
            } else if (result.getCode().equals(Result.PARAM)) {
                logger.error("withdrawBtcSubmit {}", result.getMsg());
            } else if (result.getCode() < 10000) {
                return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("capital.coin.withdraw.create." + result.getCode().toString(), result.getData().toString()));
            } else {
                return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("com.error." + result.getCode().toString(), result.getData().toString()));
            }
        } catch (Exception e) {
            logger.error("访问/v2/capital/withdraw异常",e);
        }
        return ReturnResult.FAILUER(GetR18nMsg("com.error.10117"));
    }
    
    /**
     * 验证提现密码
     * @param tradePwd   提现密码
     */
    @ResponseBody
    @RequestMapping(value = "/v2/capital/check_tradePwd_sign")
    public ReturnResult withdrawBtcSubmit(@RequestParam(required = true) String tradePwd) {
    	try {
    		
	        if (StringUtils.isEmpty(tradePwd)) {
	            return ReturnResult.FAILUER(GetR18nMsg("com.error.10116"));
	        }
	        // 用户
	        FUser user = getUser();
	        user = userService.selectUserById(user.getFid());
	        if(StringUtils.isEmpty(user.getFtradepassword())) {
	        	return ReturnResult.FAILUER(GetR18nMsg("com.error.10003"));
	        }
	        tradePwd = Utils.MD5(tradePwd);
	        Result tradePasswordCheck = validationCheckHelper.getTradePasswordCheck(user.getFtradepassword(), tradePwd, getIpAddr());
	        if(tradePasswordCheck.getSuccess()) {
	        	return ReturnResult.SUCCESS(GetR18nMsg("common.succeed.200"));
	        }
	        return ReturnResult.FAILUER(GetR18nMsg("com.error.10009", tradePasswordCheck.getData()));
        } catch (Exception e) {
            logger.error("访问/v2/capital/check_tradePwd异常",e);
        }
        return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
    }
    
}
