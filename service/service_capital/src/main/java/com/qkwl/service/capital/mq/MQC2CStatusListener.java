package com.qkwl.service.capital.mq;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.qkwl.common.rpc.c2c.C2CService;
import com.qkwl.service.capital.service.C2CServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * C2C订单状态处理
 * @author TT
 */
public class MQC2CStatusListener implements MessageListener {
	/**
	 * 日志
	 */
	private static final Logger logger = LoggerFactory.getLogger(MQC2CStatusListener.class);

	@Autowired
	private C2CService c2cService;

	@Override
	public Action consume(Message message, ConsumeContext context) {
		// body
		String body = new String(message.getBody());
		try {
			// 序列号对象
			int id = Integer.parseInt(body);
			// 参数判断
			if (body == null || id == 0) {
				logger.error("c2cStatus is null");
				return Action.ReconsumeLater;
			}
			// 删除队列
			c2cService.remove(id);
			return Action.CommitMessage;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("MQC2CStatusListener failed : {} body : {}", message.getMsgID(), body);
			return Action.ReconsumeLater;
		}
	}
}
