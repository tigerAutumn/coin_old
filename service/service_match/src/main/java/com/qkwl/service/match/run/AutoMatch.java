package com.qkwl.service.match.run;

import java.util.List;

import com.qkwl.common.dto.Enum.SystemTradeStatusEnum;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.service.match.utils.MatchUtils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qkwl.common.dto.entrust.FEntrust;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.service.match.services.MatchServiceImpl;

import javax.annotation.PostConstruct;

@Component("autoMatch")
public class AutoMatch {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(AutoMatch.class);

    private static final String MATCH_TRADE_THREAD_KEY = "MatchTradeThread";

    @Autowired
    private MatchUtils matchUtils;
    @Autowired
    private MatchServiceImpl matchService;
    @Autowired
    private RedisHelper redisHelper;
    
    private Integer[] tradeIds;

    @PostConstruct
    public void init() {
        Thread mainThread = new Thread(new Work());
        mainThread.setName("AutoMatch");
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
                	 MatchTradeThread thread = new MatchTradeThread(tradeId, matchUtils, matchService);
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
                    List<SystemTradeType> systemTradeTypes = matchUtils.getTradeTypeList();
                                    
                    if (systemTradeTypes == null) {
                        continue;
                    }
                    
                    // 遍历虚拟币列表
                    for (SystemTradeType systemTradeType : systemTradeTypes) {
                        
                        if (systemTradeType == null) {
                            continue;
                        }
                        // 非平台撮合 跳过
                        if (!systemTradeType.getStatus().equals(SystemTradeStatusEnum.NORMAL.getCode())) {
                            continue;
                        }
                        // 币种ID
                        int tradeId = systemTradeType.getId();
                        
                        //判断是否单独开线程
                        if(tradeIds != null && matchUtils.isInList(tradeIds, tradeId)) {
                        	continue;
                        }
                        

                        // 获取排序买卖单
                        List<FEntrust> buyEntrusts = matchService.getBuyEntrusts(tradeId);
                        List<FEntrust> sellEntrusts = matchService.getSellEntrusts(tradeId);
                        if (buyEntrusts == null || buyEntrusts.size() <= 0
                                || sellEntrusts == null || sellEntrusts.size() <= 0) {
                            continue;
                        }
                        

                        if (buyEntrusts.get(0).getFprize().compareTo(sellEntrusts.get(0).getFprize()) < 0) {
                            continue;
                        }
                        
                        //logger.info("Work tradeId = " + tradeId);

                        
                        // 遍历判断价格
                        for (FEntrust buyEntrustWait : buyEntrusts) {
                        	
                            for (FEntrust sellEntrustWait : sellEntrusts) {
                                if (buyEntrustWait == null || sellEntrustWait == null) {
                                    continue;
                                }
                                

                                if(MathUtils.compareTo(buyEntrustWait.getFprize(), sellEntrustWait.getFprize()) < 0) {                            	
                                	break;
                                }

                            	logger.info("Work updateMatch tradeId " + tradeId);
                                matchService.updateMatch(systemTradeType, buyEntrustWait, sellEntrustWait);
                            }
                            
                        }
                    }
				} catch (Exception e) {
					e.printStackTrace();
		            continue;
				}
            	
                
            }
        }
    }
    
    
	public class MatchTradeThread extends Thread {
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


		MatchTradeThread(Integer tradeId, MatchUtils matchUtils, MatchServiceImpl matchService){
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
					// 非平台撮合 跳过
					if (!systemTradeType.getStatus().equals(SystemTradeStatusEnum.NORMAL.getCode())) {
					    continue;
					}
					
					// 获取排序买卖单
					List<FEntrust> buyEntrusts = this.matchService.getBuyEntrusts(tradeId);
					List<FEntrust> sellEntrusts = this.matchService.getSellEntrusts(tradeId);

					
					if (buyEntrusts == null || buyEntrusts.size() <= 0
					        || sellEntrusts == null || sellEntrusts.size() <= 0) {
					    continue;
					}
					
					
					if (buyEntrusts.get(0).getFprize().compareTo(sellEntrusts.get(0).getFprize()) < 0) {
					    continue;
					}
					

					
					// 遍历判断价格
					for (FEntrust buyEntrustWait : buyEntrusts) {
					    for (FEntrust sellEntrustWait : sellEntrusts) {
					        if (buyEntrustWait == null || sellEntrustWait == null) {
					            continue;
					        }
					        
					        if(MathUtils.compareTo(buyEntrustWait.getFprize(), sellEntrustWait.getFprize()) < 0) {                            	
					        	break;
					        }
					        
					        logger.info("MatchTradeThread updateMatch tradeId " + tradeId);
					        // 意向达成,开始撮合
	                    	this.matchService.updateMatch(systemTradeType, buyEntrustWait, sellEntrustWait);
						}
					}
				} catch (Exception e) {
		            e.printStackTrace();
		            continue;
				} 
				
			 }
		}
    
	}
}
