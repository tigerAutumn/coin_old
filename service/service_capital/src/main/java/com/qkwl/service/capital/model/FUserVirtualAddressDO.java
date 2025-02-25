package com.qkwl.service.capital.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户虚拟币充值地址
 * @author LY
 */
public class FUserVirtualAddressDO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	// 主键ID
	private Integer fid;
	// 虚拟币ID
	private Integer fcoinid;
	// 虚拟币地址
	private String fadderess;
	// 用户ID
	private Integer fuid;
	// 创建时间
	private Date fcreatetime;
	//地址标签
	private String memo;
	
	private String fshortname;

	public Integer getFid() {
		return fid;
	}

	public void setFid(Integer fid) {
		this.fid = fid;
	}

	public Integer getFcoinid() {
		return fcoinid;
	}

	public void setFcoinid(Integer fcoinid) {
		this.fcoinid = fcoinid;
	}

	public String getFadderess() {
		return fadderess;
	}

	public void setFadderess(String fadderess) {
		this.fadderess = fadderess == null ? null : fadderess.trim();
	}

	public Integer getFuid() {
		return fuid;
	}

	public void setFuid(Integer fuid) {
		this.fuid = fuid;
	}

	public Date getFcreatetime() {
		return fcreatetime;
	}

	public void setFcreatetime(Date fcreatetime) {
		this.fcreatetime = fcreatetime;
	}

	public String getFshortname() {
		return fshortname;
	}

	public void setFshortname(String fshortname) {
		this.fshortname = fshortname;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	
	
}