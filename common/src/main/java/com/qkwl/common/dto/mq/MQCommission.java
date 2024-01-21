package com.qkwl.common.dto.mq;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

public class MQCommission implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4778131920495152035L;

	// 卖方手续费
	private BigDecimal sellAmountFee;
	// 卖方被邀请人id
	private Integer sellInviteeId;
	// 卖方委单id
	private BigInteger sellEntrustId;
	// 卖方状态
	private String sellStatus;
	// 买方手续费
	private BigDecimal buyCountFee;
	// 买方被邀请人id
	private Integer buyInviteeId;
	// 买方委单id
	private BigInteger buyEntrustId;
	// 买方状态
	private String buyStatus;
	
	public BigDecimal getSellAmountFee() {
		return sellAmountFee;
	}
	public void setSellAmountFee(BigDecimal sellAmountFee) {
		this.sellAmountFee = sellAmountFee;
	}
	public Integer getSellInviteeId() {
		return sellInviteeId;
	}
	public void setSellInviteeId(Integer sellInviteeId) {
		this.sellInviteeId = sellInviteeId;
	}
	public BigInteger getSellEntrustId() {
		return sellEntrustId;
	}
	public void setSellEntrustId(BigInteger sellEntrustId) {
		this.sellEntrustId = sellEntrustId;
	}
	public String getSellStatus() {
		return sellStatus;
	}
	public void setSellStatus(String sellStatus) {
		this.sellStatus = sellStatus;
	}
	public BigDecimal getBuyCountFee() {
		return buyCountFee;
	}
	public void setBuyCountFee(BigDecimal buyCountFee) {
		this.buyCountFee = buyCountFee;
	}
	public Integer getBuyInviteeId() {
		return buyInviteeId;
	}
	public void setBuyInviteeId(Integer buyInviteeId) {
		this.buyInviteeId = buyInviteeId;
	}
	public BigInteger getBuyEntrustId() {
		return buyEntrustId;
	}
	public void setBuyEntrustId(BigInteger buyEntrustId) {
		this.buyEntrustId = buyEntrustId;
	}
	public String getBuyStatus() {
		return buyStatus;
	}
	public void setBuyStatus(String buyStatus) {
		this.buyStatus = buyStatus;
	}

}
