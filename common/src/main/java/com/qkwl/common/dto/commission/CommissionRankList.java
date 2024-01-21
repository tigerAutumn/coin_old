package com.qkwl.common.dto.commission;

import java.io.Serializable;
import java.math.BigDecimal;

public class CommissionRankList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//序号
	private int orderNumber;
	//账号
	private String inviterLoginname;
	//返佣折合
	private BigDecimal sumCommission;
	
	public int getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	public String getInviterLoginname() {
		return inviterLoginname;
	}
	public void setInviterLoginname(String inviterLoginname) {
		this.inviterLoginname = inviterLoginname;
	}
	public BigDecimal getSumCommission() {
		return sumCommission;
	}
	public void setSumCommission(BigDecimal sumCommission) {
		this.sumCommission = sumCommission;
	}
}
