package com.qkwl.service.match.run;

import com.qkwl.common.dto.Enum.SystemTradeStatusEnum;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.entrust.FEntrust;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.service.match.run.AutoMatch.MatchTradeThread;
import com.qkwl.service.match.run.AutoMatch.Work;
import com.qkwl.service.match.services.MatchServiceImpl;
import com.qkwl.service.match.utils.MatchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component("autoMatchCancel")
public class AutoMatchCancel {

	/**
	 * 日志
	 */
	private static final Logger logger = LoggerFactory.getLogger(AutoMatchCancel.class);
	private static final String MATCH_TRADE_THREAD_KEY = "MatchTradeThread";
	
	@Autowired
	private MatchUtils matchUtils;
	@Autowired
	private MatchServiceImpl matchService;
	@Autowired
    private RedisHelper redisHelper;
    private Integer[] tradeIds;
		
//	@PostConstruct
//	public void init() {
//		Thread thread = new Thread(new Work());
//		thread.setName("AutoMatchCancel");
//		thread.start();
//	}
	
	@PostConstruct
    public void init() {
		Thread mainThread = new Thread(new Work());
		mainThread.setName("AutoMatchCancel");
		mainThread.setPriority(8);
		mainThread.start();
        
        String tradeThread = redisHelper.getSystemArgs(MATCH_TRADE_THREAD_KEY);

        if(tradeThread != null) {
        	 String[] tradeThreads = tradeThread.split("#");
             if (tradeThreads.length <= 0) {
                 return;
             }
             
             tradeIds = new Integer[tradeThreads.length];

             for (int i = 0; i < tradeThreads.length; i++) {
                 Integer tradeId = Integer.parseInt(tradeThreads[i]);
                 tradeIds[i] = tradeId;
                 
                 if(tradeId > 0) {
                	 CancelTradeThread thread = new CancelTradeThread(tradeId, matchUtils, matchService);
                	 thread.setName("thread-"+tradeId);
                	 thread.start();
                 }
                 
             }
        }
        
    }
	

	class Work implements Runnable {
		public void run() {
			while (true) {
				try {

					// 获取币种列表
					List<SystemTradeType> systemTradeTypes = matchUtils.getAllTradeTypeList();
					if (systemTradeTypes == null) {
						continue;
					}
					
					// 遍历虚拟币列表
					for (SystemTradeType systemTradeType : systemTradeTypes) {
						if (systemTradeType == null) {
							continue;
						}
						
						int tradeId = systemTradeType.getId();
						
						//判断是否单独开线程
	                    if(tradeIds != null && matchUtils.isInList(tradeIds, tradeId)) {
	                    	continue;
	                    }
						
						// 火币撮合跳过
						if (systemTradeType.getStatus().equals(SystemTradeStatusEnum.HUOBI.getCode())) {
							continue;
						}

						// 强事务锁单更新
						List<FEntrust> fCancelEntrusts = matchService.getWaitCancelEntrust(tradeId);
						for (FEntrust fEntrust : fCancelEntrusts) {
							matchService.updateCancelMatch(fEntrust);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				
			}
		}
	}
	
	public class CancelTradeThread extends Thread {
		private Integer tradeId;
		private MatchUtils matchUtils;
		
		private MatchServiceImpl matchService;
		
		public Integer getTradeId() {
			return tradeId;
		}
	
		public void setTradeId(Integer tradeId) {
			this.tradeId = tradeId;
		}
		public MatchUtils getMatchUtils() {
			return matchUtils;
		}

		public void setMatchUtils(MatchUtils matchUtils) {
			this.matchUtils = matchUtils;
		}

		public MatchServiceImpl getMatchService() {
			return matchService;
		}

		public void setMatchService(MatchServiceImpl matchService) {
			this.matchService = matchService;
		}


		CancelTradeThread(Integer tradeId, MatchUtils matchUtils, MatchServiceImpl matchService){
			this.tradeId = tradeId;
			this.matchUtils = matchUtils;
			this.matchService = matchService;
		}
		
		
		public void run() {
			 while (true) { 
				try {
					SystemTradeType systemTradeType = redisHelper.getTradeType(tradeId, 0);
					   
					if (systemTradeType == null) {
						continue;
					}
					// 火币撮合跳过
					if (systemTradeType.getStatus().equals(SystemTradeStatusEnum.HUOBI.getCode())) {
						continue;
					}
					// 币种ID
					int tradeId = systemTradeType.getId();

					// 强事务锁单更新
					List<FEntrust> fCancelEntrusts = matchService.getWaitCancelEntrust(tradeId);
					for (FEntrust fEntrust : fCancelEntrusts) {
						matchService.updateCancelMatch(fEntrust);
					}
				} catch (Exception e) {
		            e.printStackTrace();
		            continue;
				} 
				
			 }
		}
    
	}
}
