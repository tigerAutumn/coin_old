package com.qkwl.service.commission.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qkwl.common.dto.activity.ActivityConfig;
import com.qkwl.common.dto.commission.Commission;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.rpc.commission.ICommissionService;
import com.qkwl.service.commission.dao.ActivityConfigMapper;
import com.qkwl.service.commission.dao.CommissionMapper;

@Service("commissionService")
public class CommissionServiceImpl implements ICommissionService {

	@Autowired
	private CommissionMapper commissionMapper;
	@Autowired
	private ActivityConfigMapper activityConfigMapper;
	
	@Override
	public List<Commission> selectCommissionByIntroId(Integer inviterId) {
		return commissionMapper.selectCommissionByIntroId(inviterId);
	}
	
	@Override
	public BigDecimal selectAmountByIntroId(Integer inviterId) {
		return commissionMapper.selectAmountByIntroId(inviterId);
	}
	
	@Override
	public Pagination<Commission> selectCommissionPageList(Pagination<Commission> pageParam, Commission commission) {
		// 组装查询条件数据
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("offset", pageParam.getOffset());
		map.put("limit", pageParam.getPageSize());
		
		map.put("inviterId", commission.getInviterId());
		map.put("inviteeLoginname", commission.getInviteeLoginname());
		map.put("merchandiseTime", commission.getMerchandiseTime());
		
		// 查询总用户数
		int count = commissionMapper.countCommissionListByParam(map);
		if(count > 0) {
			// 查询用户列表
			List<Commission> commissionList = commissionMapper.getCommissionPageList(map);
			// 设置返回数据
			pageParam.setData(commissionList);
		}
		pageParam.setTotalRows(count);
		return pageParam;
	}
	
	@Override
	public ActivityConfig selectActivityById(Integer id) {
		return activityConfigMapper.selectActivityById(id);
	}
}
