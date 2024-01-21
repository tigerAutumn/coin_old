package com.qkwl.common.dto.commission;

import java.io.Serializable;
import java.util.Date;

public class Invitee implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// 昵称
	private String floginname;
	// 注册时间
	private Date fregistertime;
	// 推荐人ID
	private Integer fintrouid;
	
	public String getFloginname() {
		return floginname;
	}
	public void setFloginname(String floginname) {
		this.floginname = floginname;
	}
	public Date getFregistertime() {
		return fregistertime;
	}
	public void setFregistertime(Date fregistertime) {
		this.fregistertime = fregistertime;
	}
	public Integer getFintrouid() {
		return fintrouid;
	}
	public void setFintrouid(Integer fintrouid) {
		this.fintrouid = fintrouid;
	}
}
