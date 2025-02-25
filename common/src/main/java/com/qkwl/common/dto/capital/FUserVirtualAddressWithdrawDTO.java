package com.qkwl.common.dto.capital;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户虚拟币提现地址
 * @author LY
 */
public class FUserVirtualAddressWithdrawDTO implements Serializable {
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
	// 版本号
	private Integer version;
	// 是否初始化
	private Boolean init;
	// 备注
	private String fremark;
	//地址标签
	private String memo;
    // WEB站LOGO
    private String webLogo;
    // 简称
    private String shortName;
	

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

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Boolean getInit() {
		return init;
	}

	public void setInit(Boolean init) {
		this.init = init;
	}

	public String getFremark() {
		return fremark;
	}

	public void setFremark(String fremark) {
		this.fremark = fremark == null ? null : fremark.trim();
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getWebLogo() {
		return webLogo;
	}

	public void setWebLogo(String webLogo) {
		this.webLogo = webLogo;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
}