package com.qkwl.common.framework.mq;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import com.qkwl.common.dto.entrust.FEntrust;
import com.qkwl.common.dto.mq.MQCommission;
import com.qkwl.common.mq.MQConstant;
import com.qkwl.common.mq.MQTopic;

/**
 * 佣金队列发送公共接口
 */

public class CommissionHelper {

	/**
	 * 日志
	 */
	private static final Logger logger = LoggerFactory.getLogger(CommissionHelper.class);
	
	private Producer commissionProducer;

	public void setCommissionHelperProducer(Producer commissionProducer) {
		this.commissionProducer = commissionProducer;
	}
	
	public void SendAmountFee(BigDecimal sellAmountFee, FEntrust sellEntrust,boolean isSellFee, 
    		BigDecimal buyCountFee, FEntrust buyEntrust, boolean isBuyFee){
		MQCommission mqCommission = new MQCommission();
		if (isSellFee) {
			mqCommission.setSellAmountFee(sellAmountFee);
			mqCommission.setSellInviteeId(sellEntrust.getFuid());
			mqCommission.setSellEntrustId(sellEntrust.getFid());
			mqCommission.setBuyStatus("Y");
		}
		if (isBuyFee) {
			mqCommission.setBuyCountFee(buyCountFee);
			mqCommission.setBuyInviteeId(buyEntrust.getFuid());
			mqCommission.setBuyEntrustId(buyEntrust.getFid());
			mqCommission.setSellStatus("Y");
		}
		Message message = new Message(MQTopic.COMMISSION, MQConstant.TAG_COMMISSION,
				JSON.toJSONBytes(mqCommission));
		message.setKey("COMMISSION_" + UUID.randomUUID().toString());
		try {
			logger.info("==========================返佣队列发送key：" +message.getKey() + "==================");
			commissionProducer.send(message);
		} catch (ONSClientException e) {
			logger.error("CommissionMQ send failed");
		}
	}
}
