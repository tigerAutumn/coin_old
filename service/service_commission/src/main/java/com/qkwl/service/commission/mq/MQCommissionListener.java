package com.qkwl.service.commission.mq;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.qkwl.common.dto.activity.ActivityConfig;
import com.qkwl.common.dto.commission.Commission;
import com.qkwl.common.dto.mq.MQCommission;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.mq.MQConstant;
import com.qkwl.common.redis.RedisDBConstant;
import com.qkwl.common.util.Constant;
import com.qkwl.service.commission.dao.ActivityConfigMapper;
import com.qkwl.service.commission.dao.CommissionMapper;
import com.qkwl.service.commission.dao.SystemArgsMapper;
import com.qkwl.service.commission.dao.UserMapper;
import com.qkwl.service.commission.util.JobUtils;

/**
 * 返佣队列
 */
public class MQCommissionListener implements MessageListener {

	/**
	 * 日志
	 */
	private static final Logger logger = LoggerFactory.getLogger(MQCommissionListener.class);
	@Autowired
	private JobUtils jobUtils;
	@Autowired
	ActivityConfigMapper activityConfigMapper;
	@Autowired
	UserMapper userMapper;
	@Autowired
	SystemArgsMapper systemArgsMapper;
	@Autowired
	CommissionMapper commissionMapper;
	
	@Override
	public Action consume(Message message, ConsumeContext context) {
		// body
		String body = new String(message.getBody());
		try {
			logger.info("=====================commission消息体:" + body + "=====================");
			// 幂等判断
			String result = jobUtils.getRedisData(RedisDBConstant.REDIS_DB_MQ, message.getKey());
			if (result != null && !"".equals(result)) {
				return Action.CommitMessage;
			}
			//校验返佣活动
			ActivityConfig activity = activityConfigMapper.selectActivityById(Constant.COMMISSION_ACTIVITY);
			if (activity == null) {
			    logger.info("======================返佣活动不存在======================");
			    return Action.CommitMessage;
			}
			Date startTime = activity.getStartTime();
			Date endTime = activity.getEndTime();
			Date today = new Date();
			if (today.after(endTime) || today.before(startTime)) {
			    logger.info("======================返佣活动未进行======================");
			    return Action.CommitMessage;
			}
			// 序列号对象
			MQCommission mqCommission = JSON.parseObject(body, MQCommission.class);
			String sellStatus = mqCommission.getSellStatus();
			String buyStatus = mqCommission.getBuyStatus();
			Integer sellIntroUID = 0;
			Integer buyIntroUID = 0;
			// 卖方手续费
			if (!StringUtils.isEmpty(sellStatus)) {
				BigDecimal sellAmountFee = mqCommission.getSellAmountFee();
				BigInteger sellEntrustId = mqCommission.getSellEntrustId();
				Integer sellInviteeId = mqCommission.getSellInviteeId();
				FUser sellIntroUser = userMapper.getIntroByUID(sellInviteeId);
				if (sellIntroUser != null) {
					sellIntroUID = sellIntroUser.getFid();
				} else {
					logger.info("======================卖方推荐人不存在======================");
				}
				if (sellIntroUID != 0) {
					String sellIntroLoginName = sellIntroUser.getFloginname();
					grantCommission(sellAmountFee, sellEntrustId, sellInviteeId, sellIntroUID, sellIntroLoginName);
				}
			}
			// 买方手续费
			if (!StringUtils.isEmpty(buyStatus)) {
				BigDecimal buyAmountFee = mqCommission.getBuyCountFee();
				BigInteger buyEntrustId = mqCommission.getBuyEntrustId();
				Integer buyInviteeId = mqCommission.getBuyInviteeId();
				FUser buyIntroUser = userMapper.getIntroByUID(buyInviteeId);
				if (buyIntroUser != null) {
					buyIntroUID = buyIntroUser.getFid();
				} else {
					logger.info("======================买方推荐人不存在======================");
				}
				if (buyIntroUID != 0 && buyIntroUID != sellIntroUID) {
					String buyIntroLoginName = buyIntroUser.getFloginname();
					grantCommission(buyAmountFee, buyEntrustId, buyInviteeId, buyIntroUID, buyIntroLoginName);
				}
			}
			// 保存Redis
			String uuid = message.getKey();
			jobUtils.setRedisData(RedisDBConstant.REDIS_DB_MQ, uuid, uuid, MQConstant.MQ_EXPRIE_TIME);
			return Action.CommitMessage;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("mq commission failed : {} body : {}", message.getMsgID(), body);
			return Action.ReconsumeLater;
		}
	}
	
	private void grantCommission(BigDecimal amountFee, BigInteger entrustId, Integer inviteeId, Integer introUID, String introLoginName) {
		if (MathUtils.compareTo(amountFee,BigDecimal.ZERO)>0) {
		    FUser inviteeUser = userMapper.getLoginNameByUID(inviteeId);
		    if (inviteeUser == null){
		        logger.info("======================用户不存在======================");
		        return;
		    }
		    //注册时间与今天的间隔需小于90天
		    int days = (int) (((new Date()).getTime() - inviteeUser.getFregistertime().getTime()) / (1000*3600*24));
		    if (days > 90) {
		    	logger.info("======================用户注册时间超过90天======================");
		        return;
			}
		    String loginName = inviteeUser.getFloginname();
		    if (introUID != null && introUID.intValue() > 0){
		        //佣金返还的比例
		        String fkey = systemArgsMapper.getFvalue(Constant.COMMISSION_RATE);
		        BigDecimal commissionFee = null;
		        if (!StringUtils.isEmpty(fkey)) {
		            commissionFee = new BigDecimal(fkey);
		        }
		        if (commissionFee != null){
		            BigDecimal mul = MathUtils.mul(amountFee, commissionFee);
		            if (MathUtils.compareTo(mul,BigDecimal.ZERO)>0 && MathUtils.compareTo(mul,amountFee)<0){
		                //入佣金表commission
		                Commission commission = new Commission();
		                commission.setInviterId(introUID);
		                commission.setInviterLoginname(introLoginName);
		                commission.setInviteeId(inviteeId);
		                commission.setInviteeLoginname(loginName);
		                commission.setCommissionAmount(mul);
		                commission.setMerchandiseTime(new Date());
		                commission.setEntrustId(entrustId);
		                commission.setStatus(1);
		                commission.setCreateTime(new Date());
		                commission.setUpdateTime(new Date());
		                commissionMapper.addCommission(commission);
		                logger.info("================入佣金表成功==================");
		            }
		        }
		    }
		}
	}
}
