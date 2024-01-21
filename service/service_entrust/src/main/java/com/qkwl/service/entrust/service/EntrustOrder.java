package com.qkwl.service.entrust.service;

import com.qkwl.common.dto.Enum.EntrustSourceEnum;
import com.qkwl.common.dto.Enum.EntrustStateEnum;
import com.qkwl.common.dto.Enum.EntrustTypeEnum;
import com.qkwl.common.dto.Enum.SystemTradeStatusEnum;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.entrust.EntrustOrderDTO;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.pre.PreValidationHelper;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.framework.validate.ValidationCheckHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.entrust.IEntrustServer;
import com.qkwl.common.util.Utils;
import com.qkwl.service.common.mapper.UserCommonMapper;
import com.qkwl.service.entrust.dao.UserCoinWalletMapper;
import com.qkwl.service.entrust.model.EntrustDO;
import com.qkwl.service.entrust.tx.EntrustOrderTx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 委单接口实现
 *
 * @author LY
 */
@Service("entrustOrder")
public class EntrustOrder {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(IEntrustServer.class);
    /**
     * 卷商ID
     */
    private static final Integer AGENT_ID = 0;

    @Autowired
    private UserCommonMapper userCommonMapper;
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private PreValidationHelper preValidationHelper;
    @Autowired
    private ValidationCheckHelper validationCheckHelper;
    @Autowired
    private UserCoinWalletMapper userCoinWalletMapper;
    @Autowired
    private EntrustOrderTx entrustServerTx;

    /**
     * 创建委单
     *
     * @param entrustOrder 委单数据
     */
    public Result createEntrust(EntrustOrderDTO entrustOrder, EntrustTypeEnum entrustType) {
        if (entrustOrder.getMatchType() == null) {
            return Result.param("matchType is null");
        }
        if (entrustOrder.getUserId() == null) {
            return Result.param("userId is null");
        }
        if (entrustOrder.getTradeId() == null) {
            return Result.param("tradeId is null");
        }
        if (entrustOrder.getPrize() == null || entrustOrder.getPrize().compareTo(BigDecimal.ZERO) < 0) {
            return Result.param("prize is null");
        }
        if (entrustOrder.getCount() == null || entrustOrder.getCount().compareTo(BigDecimal.ZERO) < 0) {
            return Result.param("count is null");
        }
        FUser user = userCommonMapper.selectOneById(entrustOrder.getUserId());
        if (user == null) {
            return Result.param("user is not found");
        }
        if (preValidationHelper.validateUserStatus(user)) {
            return Result.failure(10011, "账户出现安全隐患被冻结，请联系客服");
        }

        //非api的交易才需要密码
        if (entrustOrder.getSource().getCode() != EntrustSourceEnum.API.getCode()) {
            if (redisHelper.getNeedTradePassword(user.getFid())) {
                if (preValidationHelper.validateUserTradePasswordIsSetting(user)) {
                    return Result.failure(10003, "请先设置交易密码");
                }
                if (StringUtils.isEmpty(entrustOrder.getTradePass())) {
                    return Result.failure(10116, "请输入交易密码");
                }
                Result validateResult = validationCheckHelper.getTradePasswordCheck(
                        user.getFtradepassword(), entrustOrder.getTradePass(), entrustOrder.getIp());
                if (!validateResult.getSuccess()) {
                    return validateResult;
                }
            }
        }

        SystemTradeType tradeType = redisHelper.getTradeType(entrustOrder.getTradeId(), AGENT_ID);
        if (tradeType == null) {
            return Result.failure(1000, "获取交易信息失败");
        }

        //只开放了平台交易
        if (!tradeType.getIsShare() || tradeType.getStatus() != SystemTradeStatusEnum.NORMAL.getCode()){
            return Result.failure(1009, "该交易暂未开放");
        }

//        //现阶段 如果是非GSET交易区的全部不通过
//        if(!tradeType.getIsShare() || (!tradeType.getBuyShortName().toUpperCase().equals("GSET"))){
//        }

        // 小数位处理(默认价格2位，数量4位)
        String digit = StringUtils.isEmpty(tradeType.getDigit()) ? "2#4" : tradeType.getDigit();
        String[] digits = digit.split("#");
        Integer buyScale = Integer.valueOf(digits[0]);
        Integer sellScale = Integer.valueOf(digits[1]);
        BigDecimal price = MathUtils.toScaleNum(entrustOrder.getPrize(), buyScale);
        BigDecimal count = MathUtils.toScaleNum(entrustOrder.getCount(), sellScale);
        //交易额
        BigDecimal totalTradeAmount = MathUtils.mul(price, count);
        // 交易时间段
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Time tm = Time.valueOf(sdf.format(new Date()));
        if (tradeType.getIsStop() || (tradeType.getOpenTime() != null && tm.before(tradeType.getOpenTime()))
                || (tradeType.getStopTime() != null && tm.after(tradeType.getStopTime()))) {
            return Result.failure(1002, "当前时间段停止交易");
        }
        // 价格限制
        if (tradeType.getMinPrice() != null && tradeType.getMinPrice().compareTo(BigDecimal.ZERO) > 0 && price.compareTo(tradeType.getMinPrice()) < 0) {
            return Result.failure(1003, "交易价格必须大于" + MathUtils.decimalFormat(tradeType.getMinPrice())
                    , MathUtils.decimalFormat(tradeType.getMinPrice()));
        }
        if (tradeType.getMaxPrice() != null && tradeType.getMinPrice().compareTo(BigDecimal.ZERO) > 0 && price.compareTo(tradeType.getMaxPrice()) > 0) {
            return Result.failure(1004, "交易价格必须小于" + MathUtils.decimalFormat(tradeType.getMaxPrice())
                    , MathUtils.decimalFormat(tradeType.getMaxPrice()));
        }
        // 单笔买限制
        if (tradeType.getMinCount() != null && tradeType.getMinCount().compareTo(BigDecimal.ZERO) > 0 && count.compareTo(tradeType.getMinCount()) < 0) {
            return Result.failure(1005, "交易数量必须大于" + MathUtils.decimalFormat(tradeType.getMinCount())
                    , MathUtils.decimalFormat(tradeType.getMinCount()));
        }
        if (tradeType.getMaxCount() != null && tradeType.getMaxCount().compareTo(BigDecimal.ZERO) > 0 && count.compareTo(tradeType.getMaxCount()) > 0) {
            return Result.failure(1006, "交易数量必须小于" + MathUtils.decimalFormat(tradeType.getMaxCount())
                    , MathUtils.decimalFormat(tradeType.getMaxCount()));
        }
        BigDecimal curPrice, maxPrice, priceWave;
        if (entrustType.getCode().equals(EntrustTypeEnum.BUY.getCode())) {
            // 涨停
            if (tradeType.getPriceWave() != null && tradeType.getPriceWave().compareTo(BigDecimal.ZERO) > 0) {
                curPrice = redisHelper.getKaiPrice(entrustOrder.getTradeId());
                maxPrice = MathUtils.mul(curPrice, MathUtils.add(BigDecimal.ONE, tradeType.getPriceWave()));
                priceWave = MathUtils.mul(new BigDecimal(100), MathUtils.add(BigDecimal.ONE, tradeType.getPriceWave()));
                priceWave = MathUtils.toScaleNum(priceWave, MathUtils.OTHER_SCALE);
                if (price.compareTo(maxPrice) > 0) {
                    return Result.failure(1007, "交易价格不能高于开盘价的" + MathUtils.decimalFormat(priceWave) + "%"
                            , MathUtils.decimalFormat(priceWave) + "%");
                }
            }
            // 涨幅
            if (tradeType.getPriceRange() != null && tradeType.getPriceRange().compareTo(BigDecimal.ZERO) > 0) {
                curPrice = redisHelper.getLastPrice(entrustOrder.getTradeId());
                maxPrice = MathUtils.mul(curPrice, MathUtils.add(BigDecimal.ONE, tradeType.getPriceRange()));
                priceWave = MathUtils.mul(new BigDecimal(100), MathUtils.add(BigDecimal.ONE, tradeType.getPriceRange()));
                priceWave = MathUtils.toScaleNum(priceWave, MathUtils.OTHER_SCALE);
                if (price.compareTo(maxPrice) > 0) {
                    return Result.failure(1010, "交易价格不能高于最新价的" + MathUtils.decimalFormat(priceWave) + "%"
                            , MathUtils.decimalFormat(priceWave) + "%");
                }
            }
            // 钱包余额
            UserCoinWallet userCoinWallet = userCoinWalletMapper.select(entrustOrder.getUserId(), tradeType.getBuyCoinId());
            if (userCoinWallet == null || userCoinWallet.getTotal().compareTo(totalTradeAmount) < 0) {
                return Result.failure(10118, "余额不足");
            }
        } else if (entrustType.getCode().equals(EntrustTypeEnum.SELL.getCode())) {
            // 跌停
            if (tradeType.getPriceWave() != null && tradeType.getPriceWave().compareTo(BigDecimal.ZERO) > 0) {
                curPrice = redisHelper.getKaiPrice(entrustOrder.getTradeId());
                maxPrice = MathUtils.mul(curPrice, MathUtils.sub(BigDecimal.ONE, tradeType.getPriceWave()));
                priceWave = MathUtils.mul(new BigDecimal(100), MathUtils.sub(BigDecimal.ONE, tradeType.getPriceWave()));
                priceWave = MathUtils.toScaleNum(priceWave, MathUtils.OTHER_SCALE);
                if (price.compareTo(maxPrice) < 0) {
                    return Result.failure(1008, "交易价格不能低于开盘价的" + MathUtils.decimalFormat(priceWave) + "%"
                            , MathUtils.decimalFormat(priceWave) + "%");
                }
            }
            // 跌幅
            if (tradeType.getPriceRange() != null && tradeType.getPriceRange().compareTo(BigDecimal.ZERO) > 0) {
                curPrice = redisHelper.getLastPrice(entrustOrder.getTradeId());
                maxPrice = MathUtils.mul(curPrice, MathUtils.add(BigDecimal.ONE, tradeType.getPriceRange()));
                priceWave = MathUtils.mul(new BigDecimal(100), MathUtils.sub(BigDecimal.ONE, tradeType.getPriceRange()));
                priceWave = MathUtils.toScaleNum(priceWave, MathUtils.OTHER_SCALE);
                if (price.compareTo(maxPrice) > 0) {
                    return Result.failure(1011, "交易价格不能低于最新价的" + MathUtils.decimalFormat(priceWave) + "%"
                            , MathUtils.decimalFormat(priceWave) + "%");
                }
            }
            // 钱包余额
            UserCoinWallet userCoinWallet = userCoinWalletMapper.select(entrustOrder.getUserId(), tradeType.getSellCoinId());
            if (userCoinWallet == null || userCoinWallet.getTotal().compareTo(count) < 0) {
                return Result.failure(10118, "余额不足");
            }
        }
        // 创建订单
        EntrustDO entrust = new EntrustDO();
        entrust.setFuid(entrustOrder.getUserId());
        entrust.setFtradeid(entrustOrder.getTradeId());
        entrust.setFbuycoinid(tradeType.getBuyCoinId());
        entrust.setFsellcoinid(tradeType.getSellCoinId());
        entrust.setFstatus(EntrustStateEnum.Going.getCode());
        entrust.setFtype(entrustType.getCode());
        entrust.setFprize(price);
        entrust.setFcount(count);
        entrust.setFleftcount(count);
        entrust.setFamount(totalTradeAmount);
        entrust.setFsuccessamount(BigDecimal.ZERO);
        entrust.setFfees(BigDecimal.ZERO);
        entrust.setFleftfees(BigDecimal.ZERO);
        entrust.setFmatchtype(entrustOrder.getMatchType().getCode());
        entrust.setFsource(entrustOrder.getSource().getCode());
        entrust.setFlastupdattime(Utils.getTimestamp());
        entrust.setFcreatetime(Utils.getTimestamp());
        entrust.setFlast(BigDecimal.ZERO);
        entrust.setFagentid(AGENT_ID);
        // 下单
        try {
            BigInteger entrustId = entrustServerTx.createEntrust(entrust);
            redisHelper.setNeedTradePassword(user.getFid());
            return Result.success("委托成功", entrustId);
        } catch (Exception ex) {
            logger.error("create entrust is error, EntrustOrderDTO:{}", entrustOrder.toString(), ex);
            return Result.failure("委托失败");
        }
    }
}