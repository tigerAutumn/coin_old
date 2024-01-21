package com.qkwl.service.score.job;

import com.qkwl.common.dto.user.FUser;
import com.qkwl.service.score.service.UserScoreService;
import com.qkwl.service.score.util.JobUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 定时结算积分
 *
 * @author LY
 */
//@Component("autoScoreLocal")
public class AutoScoreLocal {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(AutoScoreLocal.class);

    @Autowired
    private UserScoreService userScoreService;
    @Autowired
    private JobUtils jobUtils;

//    @Scheduled(cron = "0 0 0 1/1 * ?")
    public void work() {
        // 定时计算净资产积分
//        List<FUser> userList = userScoreService.getAllUser();
//        Map<Integer, Integer> coinIdToTradeId = jobUtils.getCoinIdToTradeId();
//        for (FUser user : userList) {
//            try {
//                userScoreService.updateUserAssetsScore(user, coinIdToTradeId);
//            } catch (Exception e) {
//                e.printStackTrace();
//                logger.error("autoscore failed : {}", user.getFid());
//                continue;
//            }
//        }
    }
}
