package com.qkwl.service.commission.job;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.qkwl.common.dto.activity.ActivityConfig;
import com.qkwl.common.dto.commission.CommissionRankList;
import com.qkwl.common.dto.commission.Invitee;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.redis.MemCache;
import com.qkwl.common.redis.RedisConstant;
import com.qkwl.common.redis.RedisObject;
import com.qkwl.common.util.AccountUtil;
import com.qkwl.common.util.Constant;
import com.qkwl.service.commission.dao.ActivityConfigMapper;
import com.qkwl.service.commission.dao.CommissionMapper;

@Component
public class CommissionJob {

	@Autowired
	ActivityConfigMapper activityConfigMapper;
	@Autowired
	CommissionMapper commissionMapper;
	@Autowired
    RedisHelper redisHelper;
	@Autowired
    private MemCache memCache;
	
	/**
	 * 日志
	 */
	private static final Logger logger = LoggerFactory.getLogger(CommissionJob.class);
	
	@Scheduled(cron="0 0 0 1/1 * ? ")
	public void work() {
		logger.info("======================返佣统计定时开始======================");
		//获取活动开始结束时间
		ActivityConfig activity = activityConfigMapper.selectActivityById(Constant.COMMISSION_ACTIVITY);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date startTime = activity.getStartTime();
		Date endTime = activity.getEndTime();
		//统计榜单
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("startTime", format.format(startTime));
		map.put("endTime", format.format(endTime));
		//统计总条数
		int rankCount = 0;
		List<CommissionRankList> rankCountList = commissionMapper.selectRankCount(map);
		if (rankCountList != null) {
			rankCount = rankCountList.size();
		}
		if (rankCount > 50) {
			rankCount = 50;
		}
		memCache.set(RedisConstant.COMMISSION + "rankCount", rankCount+"", 24*60*60);
		List<CommissionRankList> indexRankList = commissionMapper.selectIndexRankList(map);
		for (CommissionRankList commissionRankList : indexRankList) {
			commissionRankList.setInviterLoginname(AccountUtil.blurAccount(commissionRankList.getInviterLoginname()));
		}
		//统计详情页榜单，每页10条，共5页
		List<CommissionRankList> firstRankList = getRankList(map, 0);
		List<CommissionRankList> secondRankList = getRankList(map, 10);
		List<CommissionRankList> thirdRankList = getRankList(map, 20);
		List<CommissionRankList> fourthRankList = getRankList(map, 30);
		List<CommissionRankList> fifthRankList = getRankList(map, 40);
		//入Redis 设置失效时间为1天
		RedisObject newobj = new RedisObject();
		newobj.setExtObject(indexRankList);
		redisHelper.set(RedisConstant.COMMISSION + "indexRankList", newobj, 24*60*60);
		newobj.setExtObject(firstRankList);
		redisHelper.set(RedisConstant.COMMISSION + "firstRankList", newobj, 24*60*60);
		newobj.setExtObject(secondRankList);
		redisHelper.set(RedisConstant.COMMISSION + "secondRankList", newobj, 24*60*60);
		newobj.setExtObject(thirdRankList);
		redisHelper.set(RedisConstant.COMMISSION + "thirdRankList", newobj, 24*60*60);
		newobj.setExtObject(fourthRankList);
		redisHelper.set(RedisConstant.COMMISSION + "fourthRankList", newobj, 24*60*60);
		newobj.setExtObject(fifthRankList);
		redisHelper.set(RedisConstant.COMMISSION + "fifthRankList", newobj, 24*60*60);
	}
	
	//分页
	private List<CommissionRankList> getRankList(Map<String,Object> map, int offset) {
		map.put("offset", offset);
		List<CommissionRankList> rankList = commissionMapper.selectRankList(map);
		List<CommissionRankList> orderedRankList = new ArrayList<CommissionRankList>();
		for (int i = 0; i < rankList.size(); i++) {
			CommissionRankList commissionRankList = rankList.get(i);
			commissionRankList.setOrderNumber(i+offset+1);
			commissionRankList.setInviterLoginname(AccountUtil.blurAccount(commissionRankList.getInviterLoginname()));
			orderedRankList.add(commissionRankList);
		}
		return orderedRankList;
	}
}
