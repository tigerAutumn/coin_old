package com.qkwl.service.capital.impl;

import com.qkwl.common.Enum.validate.BusinessTypeEnum;
import com.qkwl.common.dto.Enum.BankInfoStatusEnum;
import com.qkwl.common.dto.Enum.LogUserActionEnum;
import com.qkwl.common.dto.Enum.SystemCoinSortEnum;
import com.qkwl.common.dto.Enum.SystemCoinStatusEnum;
import com.qkwl.common.dto.Enum.UserBankInfoDefaultEnum;
import com.qkwl.common.dto.Enum.WithdrawBankTypeEnum;
import com.qkwl.common.dto.capital.*;
import com.qkwl.common.dto.coin.FPool;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.system.FSystemBankinfoWithdraw;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.framework.pre.PreValidationHelper;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.framework.validate.ValidationCheckHelper;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.capital.IUserCapitalAccountService;
import com.qkwl.common.util.PojoConvertUtil;
import com.qkwl.common.util.Utils;
import com.qkwl.service.capital.dao.FPoolMapper;
import com.qkwl.service.capital.dao.FUserBankinfoMapper;
import com.qkwl.service.capital.dao.FUserVirtualAddressMapper;
import com.qkwl.service.capital.dao.FUserVirtualAddressWithdrawMapper;
import com.qkwl.service.capital.model.FPoolDO;
import com.qkwl.service.capital.model.FUserBankinfoDO;
import com.qkwl.service.capital.model.FUserVirtualAddressDO;
import com.qkwl.service.capital.model.FUserVirtualAddressWithdrawDO;
import com.qkwl.service.capital.tx.CoinCapitalServiceTx;
import com.qkwl.service.capital.util.MQSendUtils;
import com.qkwl.service.common.mapper.UserCommonMapper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户银行卡、地址接口实现
 */
@Service("userBandAddressService")
public class UserBankAddressServiceImpl implements IUserCapitalAccountService {

    private static final Logger logger = LoggerFactory.getLogger(UserBankAddressServiceImpl.class);

    @Autowired
    private FUserBankinfoMapper userBankInfoMapper;
    @Autowired
    private FUserVirtualAddressWithdrawMapper userVirtualAddressWithdrawMapper;
    @Autowired
    private FUserVirtualAddressMapper userVirtualAddressMapper;
    @Autowired
    private FPoolMapper poolMapper;
    @Autowired
    private CoinCapitalServiceTx coinCapitalServiceTx;
    @Autowired
    private MQSendUtils mqSendUtils;
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private UserCommonMapper userCommonMapper;
    @Autowired
    private PreValidationHelper preValidationHelper;
    @Autowired
    private ValidationCheckHelper validationCheckHelper;

    /**
    * 银行卡信息的参数验证
    * @param userBankinfo 银行信息DTO
    * @return Result   返回结果<br/>
    * 200 : 成功
    * 1003: 您没有绑定手机或谷歌验证，请去<a href\='/user/security.html'>安全中心</a>绑定手机或谷歌验证后提现！<br/>
    * 1004: 请先完成实名认证！<br/>
    * 1005: 银行卡账号名必须与您的实名认证姓名一致！<br/>
    * 1006: 您已绑定该银行卡，请勿重复绑定！<br/>
    */
    private Result validateParam(UserBankinfoDTO userBankinfo){
        if (userBankinfo.getUserId() == null) {
            return Result.param("userId is null");
        }
        if (userBankinfo.getBankNumber() == null) {
            return Result.param("bankNumber is null");
        }
        if (userBankinfo.getRealName() == null) {
            return Result.param("realName is null");
        }
        if (userBankinfo.getProv() == null) {
            return Result.param("prov is null");
        }
        if (userBankinfo.getCity() == null) {
            return Result.param("city is null");
        }
        if (userBankinfo.getAddress() == null) {
            return Result.param("address is null");
        }
        if (userBankinfo.getType() == null) {
            return Result.param("type is null");
        }
        if (userBankinfo.getIp() == null) {
            return Result.param("ip is null");
        }

        FUser user = userCommonMapper.selectOneById(userBankinfo.getUserId());
        if(user == null){
            return Result.param("find user fall");
        }
        // 绑定手机或者谷歌
        if (!user.getFgooglebind() && !user.getFistelephonebind()) {
            return Result.failure(1003,"您没有绑定手机或谷歌验证，请去<a href\\='/user/security.html'>安全中心</a>绑定手机或谷歌验证后提现！");
        }
        // 判断实名
        if(!user.getFhasrealvalidate()){
            return Result.failure(1004, "请先完成实名认证！");
        }

        if (!userBankinfo.getRealName().equals(user.getFrealname())) {
            return Result.failure(1005, "银行卡账号名必须与您的实名认证姓名一致！");
        }
        // 验证手机验证码，谷歌验证码
        Result checkResult = validationCheckHelper.getChangeCheck(user, userBankinfo.getPhoneCode(),
                BusinessTypeEnum.SMS_CNY_WITHDRAW.getCode(), userBankinfo.getTotpCode(), userBankinfo.getIp(), userBankinfo.getPlatform().getCode());
        if (!checkResult.getSuccess()) {
            return checkResult;
        }
        //如果有主键id,表示为修改银行卡
        if(userBankinfo.getId() != null && userBankinfo.getId() > 0){
            return Result.success();
        }

        Map<String,Object> param = new HashMap<>();
        param.put("banknumber",userBankinfo.getBankNumber());
        param.put("init",true);

        //判断银行卡是否存在
        if(userBankInfoMapper.getBankInfoByCount(param) > 0){
            return Result.failure(1006, "您已绑定该银行卡，请勿重复绑定！");
        }
        return Result.success();
    }

    /**
     * 添加或修改提现银行卡
     * @param userBankinfo 银行信息DTO
     * @return Result   返回结果<br/>
     * 200 : 成功
     * 1000: 提现银行卡未找到！<br/>
     * 1001: 修改失败！<br/>
     * 1002: 新增失败！<br/>
     * 1003: 您没有绑定手机或谷歌验证，请去<a href\='/user/security.html'>安全中心</a>绑定手机或谷歌验证后提现！<br/>
     * 1004: 请先完成实名认证！<br/>
     * 1005: 银行卡账号名必须与您的实名认证姓名一致！<br/>
     * 1006: 您已绑定该银行卡，请勿重复绑定！<br/>
    */
    @Override
    public Result createOrUpdateBankInfo(UserBankinfoDTO userBankinfo) {
        //参数验证，失败跳出
        Result result = validateParam(userBankinfo);
        if(!result.getSuccess()){
            return result;
        }

        String backName = "Alipay";
        if(userBankinfo.getType().equals(WithdrawBankTypeEnum.Bank.getCode())){
            FSystemBankinfoWithdraw withdraw = redisHelper.getWithdrawBank(userBankinfo.getSystemBankId());
            if (withdraw == null) {
                return Result.failure(1000, "提现账号未找到！");
            }
            backName = withdraw.getFcnname();
        }

        //新增还是修改
        boolean isUpdate = false;
        FUserBankinfoDO bankinfo = null;
        //判断组件id是否存在，存在查询记
        if(userBankinfo.getId() != null && userBankinfo.getId() > 0){
            isUpdate = true;
            bankinfo = userBankInfoMapper.selectByPrimaryKey(userBankinfo.getId());
        }
        if(bankinfo == null){
            bankinfo = new FUserBankinfoDO();
            bankinfo.setFuid(userBankinfo.getUserId());
            bankinfo.setFname(backName);
            bankinfo.setFbanknumber(userBankinfo.getBankNumber());
            bankinfo.setFbanktype(userBankinfo.getSystemBankId());
            bankinfo.setFrealname(userBankinfo.getRealName());
            bankinfo.setFtype(userBankinfo.getType());
            bankinfo.setInit(true);
            bankinfo.setFstatus(BankInfoStatusEnum.NORMAL_VALUE);
            bankinfo.setFcreatetime(new Date());
            bankinfo.setVersion(0);
        }
        bankinfo.setFprov(userBankinfo.getProv());
        bankinfo.setFcity(userBankinfo.getCity());
        bankinfo.setFdist(userBankinfo.getDist());
        bankinfo.setFaddress(userBankinfo.getAddress());

        //判断是新增还是修改
        if(isUpdate){
            if (userBankInfoMapper.updateByPrimaryKey(bankinfo) <= 0) {
                return Result.failure(1001,"修改失败！");
            }
            mqSendUtils.SendUserAction(userBankinfo.getUserId(), LogUserActionEnum.MODIFY_BANK, userBankinfo.getIp());
            return Result.success("修改成功");
        } else{
            if (userBankInfoMapper.insert(bankinfo) <= 0) {
                return Result.failure(1002,"新增失败！");
            }
            // MQ_USER_ACTION
            mqSendUtils.SendUserAction(userBankinfo.getUserId(), LogUserActionEnum.ADD_BANK, userBankinfo.getIp());
            return Result.success("新增成功");
        }
    }

    /**
     * 删除提现银行卡或支付宝
     * @param userId 用户id
     * @param bankId 提现银行卡id
     * @return Result   返回结果<br/>
     * 200 : 删除成功
     * 1000: 记录不存在！<br/>
     * 1001: 删除失败！<br/>
     */
    @Override
    public Result deleteBankInfo(Integer userId, Integer bankId) {
        if(userId == null){
            return Result.param("userId is null");
        }
        if(bankId == null){
            return Result.param("bankId is null");
        }
        FUserBankinfoDO userBankinfo = userBankInfoMapper.selectByPrimaryKey(bankId);
        if (userBankinfo == null || !userBankinfo.getFuid().equals(userId)) {
            return Result.failure(1000, "记录不存在");
        }
        userBankinfo.setInit(false);
        if (userBankInfoMapper.updateByPrimaryKey(userBankinfo) <= 0) {
            return Result.failure("删除失败");
        }
        /*if(userBankInfoMapper.delete(bankId, userId) <= 0){
            return Result.failure("删除失败");
        }*/
        return Result.success("删除成功");
    }

    @Override
    public List<FUserBankinfoDTO> listBankInfo(Integer userId, Integer coinId, Integer type) {
        List<FUserBankinfoDO> list = userBankInfoMapper.getBankInfoListByUser(userId, type);
        return PojoConvertUtil.convert(list, FUserBankinfoDTO.class);
    }

    /* (non-Javadoc)
     * @see com.qkwl.common.rpc.capital.IUserCapitalAccountService#createCoinAddressRecharge(java.lang.Integer, java.lang.Integer, java.lang.String)
     */
    @Override
    public Result createCoinAddressRecharge(Integer userId, Integer coinId, String ip) {
        if(userId == null || coinId == null || StringUtils.isEmpty(ip)){
            return Result.param("参数异常!");
        }

        FUser fuser = userCommonMapper.selectOneById(userId);
        if(fuser == null){
            return Result.param("user not found!");
        }

        SystemCoinType coinType = redisHelper.getCoinType(coinId);
        if (coinType == null || !coinType.getIsRecharge() || coinType.getStatus().equals(SystemCoinStatusEnum.ABNORMAL.getCode()) ) {
            return Result.param("coin not found or unavailable!");
        }

        if(coinType.getCoinType().equals(SystemCoinSortEnum.ICS.getCode())){
            SystemCoinType icsCoinType = redisHelper.getCoinTypeShortName("ICS");
            coinId = icsCoinType.getId();
        } else if(coinType.getCoinType().equals(SystemCoinSortEnum.MIC.getCode())){
            SystemCoinType icsCoinType = redisHelper.getCoinTypeShortName("MIC");
            coinId = icsCoinType.getId();
        } else if(coinType.getCoinType().equals(SystemCoinSortEnum.ETH.getCode())){
            SystemCoinType icsCoinType = redisHelper.getCoinTypeShortName("ETH");
            coinId = icsCoinType.getId();
        } else if(coinType.getCoinType().equals(SystemCoinSortEnum.ETC.getCode())){
            SystemCoinType icsCoinType = redisHelper.getCoinTypeShortName("ETC");
            coinId = icsCoinType.getId();
        }else if(coinType.getCoinType().equals(SystemCoinSortEnum.MOAC.getCode())){
            SystemCoinType icsCoinType = redisHelper.getCoinTypeShortName("MOAC");
            coinId = icsCoinType.getId();
        }else if(coinType.getCoinType().equals(SystemCoinSortEnum.FOD.getCode())){
            SystemCoinType icsCoinType = redisHelper.getCoinTypeShortName("FOD");
            coinId = icsCoinType.getId();
        }
        
/*        SystemCoinType icsCoinType = redisHelper.getCoinTypeByCoinSort(coinType.getCoinType());
        if(icsCoinType != null) {
        	coinId = icsCoinType.getId();
        }*/
        FUserVirtualAddressDO virtualAddress;
        if(coinType.getCoinType().equals(SystemCoinSortEnum.GXS.getCode())){
            virtualAddress = new FUserVirtualAddressDO();
            virtualAddress.setFadderess(coinType.getAccessKey());
            virtualAddress.setFuid(userId);
            virtualAddress.setFcoinid(coinType.getId());
        }else if(coinType.getCoinType().equals(SystemCoinSortEnum.EOS.getCode())){
            virtualAddress = new FUserVirtualAddressDO();
            virtualAddress.setFadderess(coinType.getEthAccount());
            virtualAddress.setFuid(userId);
            virtualAddress.setFcoinid(coinType.getId());
            virtualAddress.setMemo(userId.toString());
        }else{
            virtualAddress = userVirtualAddressMapper.selectByUserAndCoin(userId, coinId);
        }
        if (virtualAddress != null) {
            return Result.success("充值地址创建成功", PojoConvertUtil.convert(virtualAddress, FUserVirtualAddressDTO.class));
        }
        FPoolDO fpool = this.poolMapper.selectOneFpool(coinId);
        if (fpool == null) {
            return Result.failure(1000, "充值地址不足");
        }
        String address = fpool.getFaddress();
        if (address == null || address.trim().equalsIgnoreCase("null") || address.trim().equals("")) {
            return Result.failure(1000, "充值地址不足");
        }
        FUserVirtualAddressDO fvirtualaddress = new FUserVirtualAddressDO();
        fvirtualaddress.setFadderess(address);
        fvirtualaddress.setFcreatetime(Utils.getTimestamp());
        fvirtualaddress.setFuid(userId);
        fvirtualaddress.setFcoinid(coinId);
        try {
            if (coinCapitalServiceTx.createCoinAddress(fpool, fvirtualaddress)) {
                mqSendUtils.SendUserAction(userId, LogUserActionEnum.ADD_ADDRESS_RECHARGE, ip);
                return Result.success("充值地址创建成功", PojoConvertUtil.convert(fvirtualaddress, FUserVirtualAddressDTO.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("createCoinAddress err userId:{},coinId:{},ip:{} ", userId, coinId, ip);
        }
        return Result.failure(1000, "充值地址不足");
    }

    @Override
    public Result createCoinAddressWithdraw(UserVirtualAddressWithdrawDTO dto) {
        if(dto.getFuid() == null){
            return Result.param("userId is null");
        }
        if(dto.getFcoinid() == null){
            return Result.param("coinId is null");
        }
        if(dto.getFadderess() == null || "".equals(dto.getFadderess().trim())){
            return Result.param("address is null");
        }
        if(dto.getIp() == null || "".equals(dto.getIp().trim())){
            return Result.param("ip is null");
        }

        FUser user = userCommonMapper.selectOneById(dto.getFuid());
        if(user == null){
            return Result.param("user not found!");
        }
        Boolean preValidate = preValidationHelper.validateUserTradePasswordIsSetting(user);
        if(preValidate){
            return Result.failure(1001, "请先设置交易密码");
        }else{
            Result result = validationCheckHelper.getTradePasswordCheck(user.getFtradepassword(), dto.getPassword(), dto.getIp());
            if(!result.getSuccess()){
                return result;
            }
        }
        FUserVirtualAddressWithdrawDO withdraw = PojoConvertUtil.convert(dto, FUserVirtualAddressWithdrawDO.class);
        if(userVirtualAddressWithdrawMapper.insert(withdraw) <= 0){
            return Result.failure("创建失败");
        }
        // MQ_USER_ACTION
        mqSendUtils.SendUserAction(withdraw.getFuid(), LogUserActionEnum.ADD_ADDRESS_WITHDRAW, dto.getIp());
        return Result.success("创建成功");
    }

    @Override
    public Result deleteCoinAddressWithdraw(Integer userId, Integer addressId) {
        if(userId == null){
            return Result.param("userId is null");
        }
        if(addressId == null){
            return Result.param("addressId is null");
        }

        FUser user = userCommonMapper.selectOneById(userId);
        if(user == null){
            return Result.param("user not found");
        }

        FUserVirtualAddressWithdrawDO address = userVirtualAddressWithdrawMapper.selectByPrimaryKey(addressId);
        if (!address.getFuid().equals(userId)) {
            return Result.failure(1002,"地址不存在");
        }
        address.setInit(false);
        if (userVirtualAddressWithdrawMapper.updateByPrimaryKey(address) <= 0) {
            return Result.failure("删除失败");
        }
        return Result.success("删除成功");
    }

    @Override
    public List<FUserVirtualAddressWithdrawDTO> listCoinAddressWithdraw(Integer userId, Integer coinId) {
    	try {
	        FUserVirtualAddressWithdrawDO withdraw = new FUserVirtualAddressWithdrawDO();
	        withdraw.setInit(true);
	        withdraw.setFuid(userId);
	        withdraw.setFcoinid(coinId);
	        return PojoConvertUtil.convert(userVirtualAddressWithdrawMapper.getVirtualCoinWithdrawAddressList(withdraw), FUserVirtualAddressWithdrawDTO.class);
		} catch (Exception e) {
			logger.info("查询提现地址异常",e);
			return null;
		}
    }

    //新版本接口，去掉了手机验证码和谷歌验证码
	@Override
	public Result createOrUpdateBankInfoV1(UserBankinfoDTO userBankinfo) {
		//参数验证，失败跳出
        Result result = validateNewParam(userBankinfo);
        if(!result.getSuccess()){
            return result;
        }

        String backName = "Alipay";
        String logo = "";
        if(userBankinfo.getType().equals(WithdrawBankTypeEnum.Bank.getCode())){
            FSystemBankinfoWithdraw withdraw = redisHelper.getWithdrawBank(userBankinfo.getSystemBankId());
            if (withdraw == null) {
                return Result.failure(1000, "提现账号未找到！");
            }
            backName = withdraw.getFcnname();
            
            logo = withdraw.getFlogo();
        }

        //新增还是修改
        boolean isUpdate = false;
        FUserBankinfoDO bankinfo = null;
        //判断组件id是否存在，存在查询记
        if(userBankinfo.getId() != null && userBankinfo.getId() > 0){
            isUpdate = true;
            bankinfo = userBankInfoMapper.selectByPrimaryKey(userBankinfo.getId());
        }
        if(bankinfo == null){
            bankinfo = new FUserBankinfoDO();
            bankinfo.setFuid(userBankinfo.getUserId());
            
            bankinfo.setFrealname(userBankinfo.getRealName());
            bankinfo.setFtype(userBankinfo.getType());
            bankinfo.setInit(true);
            bankinfo.setFstatus(BankInfoStatusEnum.NORMAL_VALUE);
            bankinfo.setFcreatetime(new Date());
            bankinfo.setVersion(0);
        }
        bankinfo.setFbanknumber(userBankinfo.getBankNumber());
        bankinfo.setFprov(userBankinfo.getProv());
        bankinfo.setFcity(userBankinfo.getCity());
        bankinfo.setFdist(userBankinfo.getDist());
        bankinfo.setFaddress(userBankinfo.getAddress());
        bankinfo.setLogo(logo);
        bankinfo.setFname(backName);
        bankinfo.setFbanktype(userBankinfo.getSystemBankId());
        
        //判断是新增还是修改
        if(isUpdate){
            if (userBankInfoMapper.updateByPrimaryKey(bankinfo) <= 0) {
                return Result.failure(1001,"修改失败！");
            }
            mqSendUtils.SendUserAction(userBankinfo.getUserId(), LogUserActionEnum.MODIFY_BANK, userBankinfo.getIp());
            return Result.success("修改成功");
        } else{
        	//查询一遍用户是否绑定了银行卡 如果一张都没有绑定则新增的为默认银行卡
        	List<FUserBankinfoDTO> list = listBankInfo(userBankinfo.getUserId(), null, WithdrawBankTypeEnum.Bank.getCode());
        	if(list == null || list.size()==0) {
        		bankinfo.setIsDefault(UserBankInfoDefaultEnum.TRUE.getCode());
        	}else if(list.size() >= 3) {
        		return Result.failure(1003,"只能添加三張銀行卡！");
        	}
        	
            if (userBankInfoMapper.insert(bankinfo) <= 0) {
                return Result.failure(1002,"新增失败！");
            }
            // MQ_USER_ACTION
            mqSendUtils.SendUserAction(userBankinfo.getUserId(), LogUserActionEnum.ADD_BANK, userBankinfo.getIp());
            return Result.success("新增成功");
        }
	}
	
	/**
    * 银行卡信息的参数验证
    * @param userBankinfo 银行信息DTO
    * @return Result   返回结果<br/>
    * 200 : 成功
    * 1004: 请先完成实名认证！<br/>
    * 1005: 银行卡账号名必须与您的实名认证姓名一致！<br/>
    * 1006: 您已绑定该银行卡，请勿重复绑定！<br/>
    */
    private Result validateNewParam(UserBankinfoDTO userBankinfo){
        if (userBankinfo.getUserId() == null) {
            return Result.param("userId is null");
        }
        if (userBankinfo.getBankNumber() == null) {
            return Result.param("bankNumber is null");
        }
        if (userBankinfo.getRealName() == null) {
            return Result.param("realName is null");
        }
        if (userBankinfo.getProv() == null) {
            return Result.param("prov is null");
        }
        if (userBankinfo.getCity() == null) {
            return Result.param("city is null");
        }
        if (userBankinfo.getAddress() == null) {
            return Result.param("address is null");
        }
        if (userBankinfo.getType() == null) {
            return Result.param("type is null");
        }
        if (userBankinfo.getIp() == null) {
            return Result.param("ip is null");
        }

        FUser user = userCommonMapper.selectOneById(userBankinfo.getUserId());
        if(user == null){
            return Result.param("find user fall");
        }
        // 判断实名
        if(!user.getFhasrealvalidate()){
            return Result.failure(10001, "请先完成实名认证！");
        }
        
        boolean preValidate = preValidationHelper.validateUserTradePasswordIsSetting(user);
        if(preValidate){
            return Result.failure(10003, "请先设置交易密码");
        }else{
            Result result = validationCheckHelper.getTradePasswordCheck(user.getFtradepassword(), userBankinfo.getPassword(), userBankinfo.getIp());
            if(!result.getSuccess()){
                return result;
            }
        }
        
        if (!userBankinfo.getRealName().equals(user.getFrealname())) {
            return Result.failure(1005, "银行卡账号名必须与您的实名认证姓名一致！");
        }
        //如果有主键id,表示为修改银行卡
        if(userBankinfo.getId() != null && userBankinfo.getId() > 0){
            return Result.success();
        }

        Map<String,Object> param = new HashMap<>();
        param.put("banknumber",userBankinfo.getBankNumber());
        param.put("init",true);

        //判断银行卡是否存在
        if(userBankInfoMapper.getBankInfoByCount(param) > 0){
            return Result.failure(1006, "您已绑定该银行卡，请勿重复绑定！");
        }
        return Result.success();
    }

	@Override
	public Result defaultBankInfo(Integer userId, Integer id) {
		if (userId == null || userId.equals(0)) {
            return Result.param("userId is null");
        }
		
		if(id == null || id.equals(0)) {
			return Result.param("id is null");
		}
		
		FUser user = userCommonMapper.selectOneById(userId);
        if(user == null){
            return Result.param("find user fall");
        }
        
        FUserBankinfoDO bankinfo = userBankInfoMapper.selectByPrimaryKey(id);
        if(bankinfo == null){
            return Result.param("find user fall");
        }
        
        //将其他银行卡状态设置为0
        List<FUserBankinfoDO> list = userBankInfoMapper.getBankInfoListByUser(user.getFid(), WithdrawBankTypeEnum.Bank.getCode());
        if(list != null && list.size() > 0) {
        	for(FUserBankinfoDO obj : list) {
        		if(!obj.getFid().equals(id)) {
        			obj.setIsDefault(UserBankInfoDefaultEnum.FALSE.getCode());
                    userBankInfoMapper.updateByPrimaryKey(obj);
        		}
        	}
        }
        
        bankinfo.setIsDefault(UserBankInfoDefaultEnum.TRUE.getCode());
        userBankInfoMapper.updateByPrimaryKey(bankinfo);
        return Result.success("修改成功");
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(Utils.MD5("lkl12345"));
		} catch (BCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
}
