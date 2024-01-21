package com.qkwl.service.match.run;

import com.qkwl.common.dto.Enum.EntrustSourceEnum;
import com.qkwl.common.dto.Enum.SystemTradeStatusEnum;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.entrust.FEntrust;
import com.qkwl.service.match.services.MatchServiceImpl;
import com.qkwl.service.match.utils.MatchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

//@Component("autoMatchApi")
//public class AutoMatchApi {
//
//    /**
//     * 日志
//     */
//    private static final Logger logger = LoggerFactory.getLogger(AutoMatchApi.class);
//
//    @Autowired
//    private MatchUtils matchUtils;
//
//    @Autowired
//    private MatchServiceImpl matchService;
//
//    @PostConstruct
//    public void init() {
//        Thread thread = new Thread(new Work());
//        thread.setName("AutoMatchApi");
//        thread.start();
//    }
//
//    class Work implements Runnable {
//        int count = 0;
//        boolean isNoTrade = true;
//        public void run() {
//            while (true) {
//                isNoTrade = true;
//                // 获取币种列表
//                List<SystemTradeType> systemTradeTypes = matchUtils.getTradeTypeList();
//                if (systemTradeTypes == null) {
//                    continue;
//                }
//                // 遍历虚拟币列表
//                for (SystemTradeType systemTradeType : systemTradeTypes) {
//                    if (systemTradeType == null) {
//                        continue;
//                    }
//                    // 非平台撮合 跳过
//                    if (!systemTradeType.getStatus().equals(SystemTradeStatusEnum.NORMAL.getCode())) {
//                        continue;
//                    }
//                    // 币种ID
//                    int tradeId = systemTradeType.getId();
//                    // 获取排序买卖单
//                    List<FEntrust> buyEntrusts = matchService.getBuyEntrustsByApi(tradeId);
//                    List<FEntrust> sellEntrusts = matchService.getSellEntrustsByApi(tradeId);
//                    if (buyEntrusts == null || buyEntrusts.size() <= 0
//                            || sellEntrusts == null || sellEntrusts.size() <= 0) {
//                        continue;
//                    }
//                    if (buyEntrusts.get(0).getFprize().compareTo(sellEntrusts.get(0).getFprize()) < 0) {
//                        continue;
//                    }
//                    // 遍历判断价格
//                    for (FEntrust buyEntrustWait : buyEntrusts) {
//                        for (FEntrust sellEntrustWait : sellEntrusts) {
//                            // null 判断
//                            if (buyEntrustWait == null || sellEntrustWait == null) {
//                                continue;
//                            }
//                            // 意向达成,开始撮合
//                            try {
//                                if(matchService.updateMatch(systemTradeType, buyEntrustWait, sellEntrustWait)){
//                                    count ++;
//                                    isNoTrade = false;
//                                }
//                            } catch (Exception e) {
//                                logger.error("math err : {}_{}", buyEntrustWait.getFid(), sellEntrustWait.getFid());
//                                e.printStackTrace();
//                                break;
//                            }
//                        }
//                    }
//                }
//
//                //如果没有交易就休息1秒
//                if (isNoTrade) {
//                    try {
//                        Thread.sleep(500);
//                        logger.info("match info : no trade sleep 1000 millis");
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    //撮合了50笔交易之后休息0.2秒
//                    if (count >= 50) {
//                        count = 0;
//                        try {
//                            Thread.sleep(200);
//                            logger.info("match info : matched 50 orders sleep 200 millis");
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
