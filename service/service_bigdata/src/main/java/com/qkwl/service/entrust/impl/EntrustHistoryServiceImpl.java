package com.qkwl.service.entrust.impl;

import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.entrust.FEntrustHistory;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.rpc.entrust.EntrustHistoryService;
import com.qkwl.common.util.PojoConvertUtil;
import com.qkwl.service.entrust.dao.FEntrustHistoryMapper;
import com.qkwl.service.entrust.model.EntrustHistoryDO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 委单接口实现
 *
 * @author LY
 */
@Service("entrustHistoryService")
public class EntrustHistoryServiceImpl implements EntrustHistoryService {

    @Autowired
    private FEntrustHistoryMapper entrustHistoryMapper;
    
    private Logger logger = LoggerFactory.getLogger(EntrustHistoryServiceImpl.class);

    /**
     * 分页查询历史当前委单
     *
     * @param paginParam 分页实体对象
     * @param entrust    历史委单实体
     * @param stateList  委单状态列表
     */
    @Override
    public Pagination<FEntrustHistory> listEntrustHistory(Pagination<FEntrustHistory> paginParam,
                                                          FEntrustHistory entrust, List<Integer> stateList) {
    	try {
    		Map<String, Object> map = new HashMap<>();
            map.put("offset", paginParam.getOffset());
            map.put("limit", paginParam.getPageSize());
            map.put("fuid", entrust.getFuid());
            map.put("ftradeid", entrust.getFtradeid());
            map.put("fagentid", entrust.getFagentid());
            map.put("ftype", entrust.getFtype());
            map.put("stateList", stateList);
            map.put("begindate", paginParam.getBegindate());
            map.put("enddate", paginParam.getEnddate());

            List<EntrustHistoryDO> fEntrustHistories = entrustHistoryMapper.selectPageList(map);
            for (EntrustHistoryDO fEntrust : fEntrustHistories) {
                fEntrust.setFamount(MathUtils.toScaleNum(fEntrust.getFamount(), MathUtils.DEF_CNY_SCALE));
                fEntrust.setFcount(MathUtils.toScaleNum(fEntrust.getFcount(), MathUtils.DEF_COIN_SCALE));
                fEntrust.setFfees(MathUtils.toScaleNum(fEntrust.getFfees(), MathUtils.DEF_FEE_SCALE));
                fEntrust.setFlast(MathUtils.toScaleNum(fEntrust.getFlast(), MathUtils.DEF_CNY_SCALE));
                fEntrust.setFleftcount(MathUtils.toScaleNum(fEntrust.getFleftcount(), MathUtils.DEF_COIN_SCALE));
                fEntrust.setFleftfees(MathUtils.toScaleNum(fEntrust.getFleftfees(), MathUtils.DEF_FEE_SCALE));
                fEntrust.setFprize(MathUtils.toScaleNum(fEntrust.getFprize(), MathUtils.DEF_CNY_SCALE));
            }
            paginParam.setData(PojoConvertUtil.convert(fEntrustHistories, FEntrustHistory.class));
            if (!StringUtils.isEmpty(paginParam.getRedirectUrl())) {
                int count = entrustHistoryMapper.selectPageCount(map);
                paginParam.setTotalRows(count);
                paginParam.generate();
            }
            return paginParam;
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
    }

    /**
     * 根据id查询历史委单
     *
     * @param userId    用户id
     * @param entrustId 委单id
     */
    @Override
    public FEntrustHistory getEntrustHistory(Integer userId, BigInteger entrustId) {
    	try {
    		EntrustHistoryDO fEntrust = entrustHistoryMapper.selectById(userId, entrustId);
            if (fEntrust != null) {
                fEntrust.setFamount(MathUtils.toScaleNum(fEntrust.getFamount(), MathUtils.DEF_CNY_SCALE));
                fEntrust.setFcount(MathUtils.toScaleNum(fEntrust.getFcount(), MathUtils.DEF_COIN_SCALE));
                fEntrust.setFfees(MathUtils.toScaleNum(fEntrust.getFfees(), MathUtils.DEF_FEE_SCALE));
                fEntrust.setFlast(MathUtils.toScaleNum(fEntrust.getFlast(), MathUtils.DEF_CNY_SCALE));
                fEntrust.setFleftcount(MathUtils.toScaleNum(fEntrust.getFleftcount(), MathUtils.DEF_COIN_SCALE));
                fEntrust.setFleftfees(MathUtils.toScaleNum(fEntrust.getFleftfees(), MathUtils.DEF_FEE_SCALE));
                fEntrust.setFprize(MathUtils.toScaleNum(fEntrust.getFprize(), MathUtils.DEF_CNY_SCALE));
            }
            return PojoConvertUtil.convert(fEntrust, FEntrustHistory.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
        
    }
    
    /**
     * 根据id查询历史委单
     *
     * @param userId    用户id
     * @param entrustId 委单id
     */
    @Override
    public FEntrustHistory getEntrustHistoryByEntrustId(Integer userId, BigInteger entrustId) {
    	try {
    		EntrustHistoryDO fEntrust = entrustHistoryMapper.selectByEntrustId(userId, entrustId);
            if (fEntrust != null) {
                fEntrust.setFamount(MathUtils.toScaleNum(fEntrust.getFamount(), MathUtils.DEF_CNY_SCALE));
                fEntrust.setFcount(MathUtils.toScaleNum(fEntrust.getFcount(), MathUtils.DEF_COIN_SCALE));
                fEntrust.setFfees(MathUtils.toScaleNum(fEntrust.getFfees(), MathUtils.DEF_FEE_SCALE));
                fEntrust.setFlast(MathUtils.toScaleNum(fEntrust.getFlast(), MathUtils.DEF_CNY_SCALE));
                fEntrust.setFleftcount(MathUtils.toScaleNum(fEntrust.getFleftcount(), MathUtils.DEF_COIN_SCALE));
                fEntrust.setFleftfees(MathUtils.toScaleNum(fEntrust.getFleftfees(), MathUtils.DEF_FEE_SCALE));
                fEntrust.setFprize(MathUtils.toScaleNum(fEntrust.getFprize(), MathUtils.DEF_CNY_SCALE));
            }
            return PojoConvertUtil.convert(fEntrust, FEntrustHistory.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
}