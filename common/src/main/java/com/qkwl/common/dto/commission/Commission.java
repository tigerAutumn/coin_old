package com.qkwl.common.dto.commission;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class Commission implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	//邀请人ID
	private Integer inviterId;
	//邀请人手机号
	private String inviterLoginname;  
	//被邀请人ID
	private Integer inviteeId;
	//被邀请人手机号
	private String inviteeLoginname;
	//返佣金额
	private BigDecimal commissionAmount;  
	//交易时间
	private Date merchandiseTime;  
	//委单ID
	private BigInteger entrustId;
	//发放状态
	private Integer status;
	//创建时间
	private Date createTime;
	//修改时间
	private Date updateTime;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getInviterId() {
		return inviterId;
	}
	public void setInviterId(Integer inviterId) {
		this.inviterId = inviterId;
	}
	public String getInviterLoginname() {
		return inviterLoginname;
	}
	public void setInviterLoginname(String inviterLoginname) {
		this.inviterLoginname = inviterLoginname;
	}
	public Integer getInviteeId() {
		return inviteeId;
	}
	public void setInviteeId(Integer inviteeId) {
		this.inviteeId = inviteeId;
	}
	public String getInviteeLoginname() {
		return inviteeLoginname;
	}
	public void setInviteeLoginname(String inviteeLoginname) {
		this.inviteeLoginname = inviteeLoginname;
	}
	public BigDecimal getCommissionAmount() {
		return commissionAmount;
	}
	public void setCommissionAmount(BigDecimal commissionAmount) {
		this.commissionAmount = commissionAmount;
	}
	public Date getMerchandiseTime() {
		return merchandiseTime;
	}
	public void setMerchandiseTime(Date merchandiseTime) {
		this.merchandiseTime = merchandiseTime;
	}
	public BigInteger getEntrustId() {
		return entrustId;
	}
	public void setEntrustId(BigInteger entrustId) {
		this.entrustId = entrustId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
}
