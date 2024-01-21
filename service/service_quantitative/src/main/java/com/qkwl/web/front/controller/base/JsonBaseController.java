package com.qkwl.web.front.controller.base;

import com.qkwl.common.Enum.validate.LocaleEnum;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.i18n.LuangeHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.util.Utils;
import com.qkwl.web.utils.WebConstant;

import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
            if (coinWallet.getCoinId().equals(0)) {
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
            if (coinWallet.getCoinId().equals(0)) {
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


}
