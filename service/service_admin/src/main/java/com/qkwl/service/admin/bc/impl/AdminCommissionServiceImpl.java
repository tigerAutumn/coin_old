package com.qkwl.service.admin.bc.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.qkwl.common.dto.commission.Commission;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.rpc.admin.IAdminCommissionService;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.service.admin.bc.dao.CommissionMapper;
import com.qkwl.service.admin.bc.dao.UserCoinWalletMapper;


/**
 * 后台用户接口实现
 */
@Service("adminCommissionService")
public class AdminCommissionServiceImpl implements IAdminCommissionService {

	private static final Logger logger = LoggerFactory.getLogger(AdminCommissionServiceImpl.class);
	
	@Autowired
	private CommissionMapper commissionMapper;
	@Autowired
    private UserCoinWalletMapper userCoinWalletMapper;
	
	@Override
	public Pagination<Commission> selectCommissionPageList(Pagination<Commission> pageParam, Commission commission) {
		// 组装查询条件数据
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("offset", pageParam.getOffset());
		map.put("limit", pageParam.getPageSize());
		map.put("orderField", pageParam.getOrderField());
		map.put("orderDirection", pageParam.getOrderDirection());
		
		map.put("inviterId", commission.getInviterId());
		map.put("inviterLoginname", commission.getInviterLoginname());
		map.put("inviteeId", commission.getInviteeId());
		map.put("inviteeLoginname", commission.getInviteeLoginname());
		map.put("status", commission.getStatus());
		
		map.put("beginDate", pageParam.getBegindate());
		map.put("endDate", pageParam.getEnddate());
		// 查询总返佣数
		int count = commissionMapper.countCommissionListByParam(map);
		if(count > 0) {
			// 查询返佣列表
			List<Commission> commissionList = commissionMapper.getCommissionPageList(map);
			// 设置返回数据
			pageParam.setData(commissionList);
		}
		pageParam.setTotalRows(count);
		return pageParam;
	}
	
	@Override
	public Commission selectCommissionById(Integer id) {
		return commissionMapper.getCommissionById(id);
	}

	@Override
	public ReturnResult grantCommission(Commission commission) {
		try {
			return grantCommissionTx(commission);
		} catch (Exception e) {
			logger.error("佣金发放异常，id："+commission.getId(),e);
			return ReturnResult.FAILUER("佣金发放失败！");
		}
	}
	
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation=Propagation.REQUIRED, rollbackFor = Exception.class)
	public ReturnResult grantCommissionTx(Commission commission) throws BCException {
		//查找用户钱包
		UserCoinWallet wallet = userCoinWalletMapper.selectByUidAndCoinLock(commission.getInviterId(), 9);
		wallet.setTotal(wallet.getTotal().add(commission.getCommissionAmount()));
		//发放佣金
		if(userCoinWalletMapper.updateByPrimaryKey(wallet) <= 0 ) {
			throw new BCException("佣金发放失败，更新数据库失败！");
		}
		//更新佣金表状态
		commission.setStatus(2);
		commissionMapper.updateCommission(commission);
		return ReturnResult.SUCCESS("佣金发放成功！");
	}
	
	@Override
	public ReturnResult forbbinGrant(Commission commission) {
		try {
			//更新佣金表状态
			commission.setStatus(3);
			commissionMapper.updateCommission(commission);
			return ReturnResult.SUCCESS("禁止发放成功！");
		} catch (Exception e) {
			logger.error("禁止发放异常，id："+commission.getId(),e);
			return ReturnResult.FAILUER("禁止发放失败！");
		}
	}
	
	@Override
	public ReturnResult relieveForbbin(Commission commission) {
		try {
			//更新佣金表状态
			commission.setStatus(1);
			commissionMapper.updateCommission(commission);
			return ReturnResult.SUCCESS("解除禁止成功！");
		} catch (Exception e) {
			logger.error("解除禁止异常，id："+commission.getId(),e);
			return ReturnResult.FAILUER("解除禁止失败！");
		}
	}
	
	@Override
	public BigDecimal selectIssuedAmountByIntroId(Integer inviterId) {
		return commissionMapper.selectIssuedAmountByIntroId(inviterId);
	}
}
