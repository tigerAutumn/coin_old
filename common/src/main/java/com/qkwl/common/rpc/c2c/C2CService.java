package com.qkwl.common.rpc.c2c;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import com.qkwl.common.dto.c2c.C2CBusiness;
import com.qkwl.common.dto.c2c.SystemC2CSetting;
import com.qkwl.common.dto.c2c.UserC2CEntrust;
import com.qkwl.common.result.Result;

public interface C2CService {
	//根据类型查询商户
	List<C2CBusiness> selectBusinessByType(int type,int coinId);
	
	//根据id查询商户
	C2CBusiness selectBusiness(int id);
	
	//分页查询用户订单
	PageInfo<UserC2CEntrust> selectList(UserC2CEntrust record,int pageNo,int pageSize);
	
	//查询系统配置
	SystemC2CSetting selectById(int id);
	
	//查询订单明细
	UserC2CEntrust selectOrderById(int id);
	
	Result createEntrust(UserC2CEntrust record);
	
	Map<String, String> getParam();
	
	void add(int orderId);
	
	void remove(int orderId);
	
	boolean updateEntrust(int orderId);
	
	int getEntrustCount(UserC2CEntrust record);
	
	boolean cancelEntrust(int orderId);
}
