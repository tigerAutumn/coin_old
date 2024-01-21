package com.qkwl.common.dto.wallet;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户钱包
 * @author LY
 *
 */
public class UserCoinWallet implements Serializable{
	
	private static final long serialVersionUID = 1L;
	// 主键ID
	private Integer id;
	// 用户ID
    private Integer uid;
    // 币种ID
    private Integer coinId;
    // 可用
    private BigDecimal total;
    // 冻结
    private BigDecimal frozen;
    // 理财
    private BigDecimal borrow;
    // ico
    private BigDecimal ico;
    // 创建时间
    private Date gmtCreate;
    // 更新时间
    private Date gmtModified;
    
    // WEB站LOGO
    private String webLogo;
    
    //是否使用地址标签
    private Boolean isUseMemo;
    
    // 扩展字段
    private String loginName;
    private String nickName;
    private String realName;
    private String coinName;
    private String shortName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Integer getCoinId() {
        return coinId;
    }

    public void setCoinId(Integer coinId) {
        this.coinId = coinId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getFrozen() {
        return frozen;
    }

    public void setFrozen(BigDecimal frozen) {
        this.frozen = frozen;
    }

    public BigDecimal getBorrow() {
        return borrow;
    }

    public void setBorrow(BigDecimal borrow) {
        this.borrow = borrow;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }



    public BigDecimal getIco() {
        return ico;
    }

    public void setIco(BigDecimal ico) {
        this.ico = ico;
    }

	public String getWebLogo() {
		return webLogo;
	}

	public void setWebLogo(String webLogo) {
		this.webLogo = webLogo;
	}

	public Boolean getIsUseMemo() {
		if(isUseMemo == null) {
			return false;
		}
		return isUseMemo;
	}

	public void setIsUseMemo(Boolean isUseMemo) {
		this.isUseMemo = isUseMemo;
	}

    
    
}