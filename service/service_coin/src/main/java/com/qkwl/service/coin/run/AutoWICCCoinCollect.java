package com.qkwl.service.coin.run;

import com.qkwl.common.dto.Enum.SystemCoinSortEnum;
import com.qkwl.common.dto.Enum.SystemCoinStatusEnum;
import com.qkwl.common.dto.Enum.SystemCoinTypeEnum;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.service.coin.service.CoinService;
import com.qkwl.service.coin.util.JobUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component("autoWICCCoinCollect")
public class AutoWICCCoinCollect {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(AutoWICCCoinCollect.class);
    @Autowired
    private CoinService coinService;
    @Autowired
    private JobUtils jobUtils;

    /**
     * 每30分钟执行一次
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void init() {
        Thread thread = new Thread(new Work());
        thread.setName("autoWICCCoinCollect");
        thread.start();
    }

    /**
     * Work
     * 将wicc的所有地址的币都集中到一个主账户
     */
    class Work implements Runnable {
        public void run() {
            SystemCoinType coinType = jobUtils.getCoinType("WICC");
            if (coinType == null) {
                return;
            }
            if(coinType.getCoinType() != SystemCoinSortEnum.WICC.getCode()) {
            	return;
            }
            if (!coinType.getType().equals(SystemCoinTypeEnum.COIN.getCode())) {
            	return;
            }
            try {
                coinService.updateWICCCoinCollect(coinType);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("updateWICCCoinCollect failed",e);
            }
        }
    }
}
