package com.qkwl.service.capital.service;

import com.qkwl.common.Enum.validate.BusinessTypeEnum;
import com.qkwl.common.Enum.validate.PlatformEnum;
import com.qkwl.common.dto.Enum.LogUserActionEnum;
import com.qkwl.common.dto.Enum.VirtualCapitalOperationInStatusEnum;
import com.qkwl.common.dto.Enum.VirtualCapitalOperationOutStatusEnum;
import com.qkwl.common.dto.capital.CoinOperationOrderDTO;
import com.qkwl.common.dto.coin.SystemCoinSetting;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.framework.validate.ValidateHelper;
import com.qkwl.common.framework.validate.ValidationCheckHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.result.Result;
import com.qkwl.common.util.ArgsConstant;
import com.qkwl.service.capital.dao.FUserVirtualAddressMapper;
import com.qkwl.service.capital.dao.FUserVirtualAddressWithdrawMapper;
import com.qkwl.service.capital.dao.FVirtualCapitalOperationMapper;
import com.qkwl.service.capital.dao.UserCoinWalletMapper;
import com.qkwl.service.capital.model.FUserVirtualAddressDO;
import com.qkwl.service.capital.model.FUserVirtualAddressWithdrawDO;
import com.qkwl.service.capital.model.FVirtualCapitalOperationDO;
import com.qkwl.service.capital.tx.CoinCapitalServiceTx;
import com.qkwl.service.capital.util.MQSendUtils;
import com.qkwl.service.common.mapper.UserCommonMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户资产计算接口实现
 *
 * @author ZKF
 */
@Service
public class CoinCapitalService {

    private static final Logger logger = LoggerFactory.getLogger(CoinCapitalService.class);

    @Autowired
    private UserCoinWalletMapper userCoinWalletMapper;
    @Autowired
    private FVirtualCapitalOperationMapper virtualCapitalOperationMapper;
    @Autowired
    private FUserVirtualAddressMapper fUserVirtualAddressMapper;
    @Autowired
    private FUserVirtualAddressWithdrawMapper userVirtualAddressWithdrawMapper;
    @Autowired
    private UserCommonMapper userCommonMapper;
    @Autowired
    private CoinCapitalServiceTx coinCapitalServiceTx;
    @Autowired
    private MQSendUtils mqSend;
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private ValidateHelper validateHelper;
    @Autowired
    private ValidationCheckHelper validationCheckHelper;

    /**
     * 充值
     *
     * @param order 订单实体
     * @return Result
     */
    public Result createRechargeOrder(CoinOperationOrderDTO order) {
        FVirtualCapitalOperationDO oldOperation = virtualCapitalOperationMapper.selectByTxid(order.getTxId());
        if (oldOperation != null) {
            return Result.failure(1000, "充值订单已存在");
        }
        FVirtualCapitalOperationDO operation = new FVirtualCapitalOperationDO();
        FUserVirtualAddressDO fvirtualaddresses = fUserVirtualAddressMapper.selectByCoinAndAddress(order.getBaseCoinId(), order.getAddress());
        FUser user = null;
        if (fvirtualaddresses == null) {
            operation.setFhasowner(false);// 没有这个地址，充错进来了？没收！
        } else {
            user = userCommonMapper.selectOneById(fvirtualaddresses.getFuid());
            operation.setFuid(fvirtualaddresses.getFuid());
            operation.setFhasowner(true);
        }
        operation.setFamount(order.getAmount());
        operation.setFfees(BigDecimal.ZERO);
        operation.setFbtcfees(BigDecimal.ZERO);
        operation.setFuniquenumber(order.getTxId());
        operation.setFrechargeaddress(order.getAddress());
        operation.setFstatus(VirtualCapitalOperationInStatusEnum.WAIT_0);
        // 通用附加
        operation.setFcoinid(order.getCoinId());
        operation.setFtype(order.getOperationType().getCode());
        operation.setFsource(order.getDataSource().getCode());
        operation.setFplatform(order.getPlatform().getCode());
        operation.setFcreatetime(new Date());
        operation.setFupdatetime(new Date());
        operation.setFblocknumber(0);
        operation.setFconfirmations(0);
        operation.setFagentid(0);
        operation.setVersion(0);
        try {
            if (coinCapitalServiceTx.createRechargeOrder(operation)) {
                if (order.getRisk() && user != null) {
                    String riskphone = redisHelper.getSystemArgs(ArgsConstant.RISKPHONE);
                    String[] riskphones = riskphone.split("#");
                    if (riskphones.length > 0) {
                        for (String phone : riskphones) {
                            validateHelper.smsRiskManage(user.getFloginname(), phone, PlatformEnum.BC.getCode(),
                                    BusinessTypeEnum.SMS_RISKMANAGE.getCode(), order.getOperationType().getValue(),
                                    order.getAmount(), order.getCoinName());
                        }
                    }
                }
                return Result.success("订单创建成功");
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("createRechargeOrder err {} ", order.toString(), e);
            }
        }
        return Result.failure("订单创建失败");
    }

    /**
     * 提现（旧版）
     *
     * @param order 订单实体
     * @return Result
     */
    public Result createWithdrawOrder(CoinOperationOrderDTO order, FUser user) {
        SystemCoinType coinType = redisHelper.getCoinTypeSystem(order.getCoinId());
        if(coinType == null){
            return Result.failure(400, "币种为空");
        }
        Integer userVipLevel = userCommonMapper.selectVipLevel(order.getUserId());
        SystemCoinSetting coinSetting = redisHelper.getCoinSetting(order.getCoinId(), userVipLevel);
        if (coinSetting == null) {
            return Result.failure(400, "币种设置为空");
        }
        if (coinSetting.getWithdrawMin() != null && coinSetting.getWithdrawMin().compareTo(BigDecimal.ZERO) > 0
                && order.getAmount().compareTo(coinSetting.getWithdrawMin()) < 0) {
            return Result.failure(1001, "最小提现数量为" + MathUtils.decimalFormat(coinSetting.getWithdrawMin()) + "",
                    MathUtils.decimalFormat(coinSetting.getWithdrawMin()));
        }
        if (coinSetting.getWithdrawMax() != null && coinSetting.getWithdrawMax().compareTo(BigDecimal.ZERO) > 0
                && order.getAmount().compareTo(coinSetting.getWithdrawMax()) > 0) {
            return Result.failure(1002, "最大提现数量为" + MathUtils.decimalFormat(coinSetting.getWithdrawMax()) + "",
                    MathUtils.decimalFormat(coinSetting.getWithdrawMax()));
        }
        BigDecimal dayWithdraw = virtualCapitalOperationMapper.getDayWithdrawCoin(order.getUserId(), order.getCoinId());
        BigDecimal blaneWithdraw = MathUtils.toScaleNum(MathUtils.sub(coinSetting.getWithdrawDayLimit(), dayWithdraw), MathUtils.ENTER_COIN_SCALE);
        if (coinSetting.getWithdrawDayLimit() != null && coinSetting.getWithdrawDayLimit().compareTo(BigDecimal.ZERO) > 0
                && MathUtils.add(dayWithdraw, order.getAmount()).compareTo(coinSetting.getWithdrawDayLimit()) > 0) {
            return Result.failure(1004, "您今日总共还可提现虚拟币" + MathUtils.decimalFormat(blaneWithdraw) + "个",
                    MathUtils.decimalFormat(blaneWithdraw));
        }
        UserCoinWallet wallet = userCoinWalletMapper.selectByUidAndCoin(order.getUserId(), order.getCoinId());
        if (wallet == null || wallet.getTotal().compareTo(order.getAmount()) == -1) {
            return Result.failure(10118, "余额不足");
        }
        FUserVirtualAddressWithdrawDO addressWithdraw = userVirtualAddressWithdrawMapper.selectByPrimaryKey(order.getAddressBindId());
        if (addressWithdraw == null) {
            return Result.failure(1005, "提币数量不足手续费");
        }
        //校验谷歌和手机验证码
        Result validateResult = validationCheckHelper.getCapitalCheck(user, order.getPhoneCode(),
                BusinessTypeEnum.SMS_CNY_WITHDRAW.getCode(),order.getGoogleCode(),
                order.getTradePass(),order.getIp(),order.getPlatform().getCode());
        
        /*Result validateResult = validationCheckHelper.getCapitalCombinationCheck(user, order.getPhoneCode(),order.getEmailCode(),order.getGoogleCode(),
                order.getTradePass(),order.getIp(),order.getPlatform().getCode());*/
        
        if(!validateResult.getSuccess()){
            return validateResult;
        }
        BigDecimal fees = coinSetting.calculateFee(order.getAmount());
        BigDecimal amount = MathUtils.sub(MathUtils.sub(order.getAmount(), order.getNetworkFees()), fees);
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
        	return Result.failure(1006, "提现地址错误");
        }
        if(coinType.getShortName().equals("BTK")){
            amount = MathUtils.toScaleNum(amount, MathUtils.INTEGER_SCALE);
        }else{
            amount = MathUtils.toScaleNum(amount, MathUtils.ENTER_COIN_SCALE);
        }

        // 数据组装
        FVirtualCapitalOperationDO operation = new FVirtualCapitalOperationDO();
        operation.setFamount(amount);
        operation.setFfees(fees);
        operation.setFwithdrawaddress(addressWithdraw.getFadderess());
        operation.setFbtcfees(order.getNetworkFees());
        operation.setFstatus(VirtualCapitalOperationOutStatusEnum.WaitForOperation);
        operation.setFhasowner(false);
        operation.setMemo(order.getMemo());
        // 通用附加
        operation.setFuid(order.getUserId());
        operation.setFcoinid(order.getCoinId());
        operation.setFtype(order.getOperationType().getCode());
        operation.setFsource(order.getDataSource().getCode());
        operation.setFplatform(order.getPlatform().getCode());
        operation.setFcreatetime(new Date());
        operation.setFupdatetime(new Date());
        operation.setFblocknumber(0);
        operation.setFconfirmations(0);
        operation.setFagentid(0);
        operation.setVersion(0);
        try {
            if (coinCapitalServiceTx.createWithdrawOrder(operation)) {
                mqSend.SendUserAction(operation.getFuid(), LogUserActionEnum.COIN_WITHDRAW_WAIT, order.getCoinId(), operation.getFamount(), order.getIp());
                if (order.getRisk()) {
                    String riskphone = redisHelper.getSystemArgs(ArgsConstant.RISKPHONE);
                    String[] riskphones = riskphone.split("#");
                    if (riskphones.length > 0) {
                        for (String phone : riskphones) {
                            validateHelper.smsRiskManage(user.getFloginname(), phone, PlatformEnum.BC.getCode(),
                                    BusinessTypeEnum.SMS_RISKMANAGE.getCode(), order.getOperationType().getValue(),
                                    order.getAmount(), order.getCoinName());
                        }
                    }
                }
                return Result.success("订单创建成功");
            }
        } catch (Exception e) {
            logger.error("createWithdrawOrder err {} ", order.toString(), e);
        }
        return Result.failure("订单创建失败");
    }
    
    /**
     * 提现（新版）
     *
     * @param order 订单实体
     * @return Result
     */
    public Result createWithdraw(CoinOperationOrderDTO order, FUser user) {
        SystemCoinType coinType = redisHelper.getCoinTypeSystem(order.getCoinId());
        if(coinType == null){
            return Result.failure(400, "币种为空");
        }
        Integer userVipLevel = userCommonMapper.selectVipLevel(order.getUserId());
        SystemCoinSetting coinSetting = redisHelper.getCoinSetting(order.getCoinId(), userVipLevel);
        if (coinSetting == null) {
            return Result.failure(400, "币种设置为空");
        }
        if (coinSetting.getWithdrawMin() != null && coinSetting.getWithdrawMin().compareTo(BigDecimal.ZERO) > 0
                && order.getAmount().compareTo(coinSetting.getWithdrawMin()) < 0) {
            return Result.failure(1001, "最小提现数量为" + MathUtils.decimalFormat(coinSetting.getWithdrawMin()) + "",
                    MathUtils.decimalFormat(coinSetting.getWithdrawMin()));
        }
        if (coinSetting.getWithdrawMax() != null && coinSetting.getWithdrawMax().compareTo(BigDecimal.ZERO) > 0
                && order.getAmount().compareTo(coinSetting.getWithdrawMax()) > 0) {
            return Result.failure(1002, "最大提现数量为" + MathUtils.decimalFormat(coinSetting.getWithdrawMax()) + "",
                    MathUtils.decimalFormat(coinSetting.getWithdrawMax()));
        }
        BigDecimal dayWithdraw = virtualCapitalOperationMapper.getDayWithdrawCoin(order.getUserId(), order.getCoinId());
        BigDecimal blaneWithdraw = MathUtils.toScaleNum(MathUtils.sub(coinSetting.getWithdrawDayLimit(), dayWithdraw), MathUtils.ENTER_COIN_SCALE);
        if (coinSetting.getWithdrawDayLimit() != null && coinSetting.getWithdrawDayLimit().compareTo(BigDecimal.ZERO) > 0
                && MathUtils.add(dayWithdraw, order.getAmount()).compareTo(coinSetting.getWithdrawDayLimit()) > 0) {
            return Result.failure(1004, "您今日总共还可提现虚拟币" + MathUtils.decimalFormat(blaneWithdraw) + "个",
                    MathUtils.decimalFormat(blaneWithdraw));
        }
        UserCoinWallet wallet = userCoinWalletMapper.selectByUidAndCoin(order.getUserId(), order.getCoinId());
        if (wallet == null || wallet.getTotal().compareTo(order.getAmount()) == -1) {
            return Result.failure(10118, "余额不足");
        }
        FUserVirtualAddressWithdrawDO addressWithdraw = userVirtualAddressWithdrawMapper.selectByPrimaryKeyAndUid(order.getAddressBindId(),user.getFid());
        if (addressWithdraw == null) {
            return Result.failure(1005, "提现地址错误");
        }
        //校验谷歌和手机验证码
        /*Result validateResult = validationCheckHelper.getCapitalCheck(user, order.getPhoneCode(),
                BusinessTypeEnum.SMS_CNY_WITHDRAW.getCode(),order.getGoogleCode(),
                order.getTradePass(),order.getIp(),order.getPlatform().getCode());*/
        Result validateResult = validationCheckHelper.getCapitalCombinationCheck(user, order.getPhoneCode(),order.getEmailCode(),order.getGoogleCode(),
                order.getTradePass(),order.getIp(),order.getPlatform().getCode());
        
        if(!validateResult.getSuccess()){
            return validateResult;
        }
        BigDecimal fees = coinSetting.calculateFee(order.getAmount());
        BigDecimal amount = MathUtils.sub(MathUtils.sub(order.getAmount(), order.getNetworkFees()), fees);
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
        	return Result.failure(1006, "提币数量不足手续费");
        }
        if(coinType.getShortName().equals("BTK")){
            amount = MathUtils.toScaleNum(amount, MathUtils.INTEGER_SCALE);
        }else{
            amount = MathUtils.toScaleNum(amount, MathUtils.ENTER_COIN_SCALE);
        }

        // 数据组装
        FVirtualCapitalOperationDO operation = new FVirtualCapitalOperationDO();
        operation.setFamount(amount);
        operation.setFfees(fees);
        operation.setFwithdrawaddress(addressWithdraw.getFadderess());
        operation.setFbtcfees(order.getNetworkFees());
        operation.setFstatus(VirtualCapitalOperationOutStatusEnum.WaitForOperation);
        operation.setFhasowner(false);
        operation.setMemo(order.getMemo());
        // 通用附加
        operation.setFuid(order.getUserId());
        operation.setFcoinid(order.getCoinId());
        operation.setFtype(order.getOperationType().getCode());
        operation.setFsource(order.getDataSource().getCode());
        operation.setFplatform(order.getPlatform().getCode());
        operation.setFcreatetime(new Date());
        operation.setFupdatetime(new Date());
        operation.setFblocknumber(0);
        operation.setFconfirmations(0);
        operation.setFagentid(0);
        operation.setVersion(0);
        try {
            if (coinCapitalServiceTx.createWithdrawOrder(operation)) {
                mqSend.SendUserAction(operation.getFuid(), LogUserActionEnum.COIN_WITHDRAW_WAIT, order.getCoinId(), operation.getFamount(), order.getIp());
                if (order.getRisk()) {
                    String riskphone = redisHelper.getSystemArgs(ArgsConstant.RISKPHONE);
                    String[] riskphones = riskphone.split("#");
                    if (riskphones.length > 0) {
                        for (String phone : riskphones) {
                            validateHelper.smsRiskManage(user.getFloginname(), phone, PlatformEnum.BC.getCode(),
                                    BusinessTypeEnum.SMS_RISKMANAGE.getCode(), order.getOperationType().getValue(),
                                    order.getAmount(), order.getCoinName());
                        }
                    }
                }
                return Result.success("订单创建成功");
            }
        } catch (Exception e) {
            logger.error("createWithdrawOrder err {} ", order.toString(), e);
        }
        return Result.failure("订单创建失败");
    }
    
}
