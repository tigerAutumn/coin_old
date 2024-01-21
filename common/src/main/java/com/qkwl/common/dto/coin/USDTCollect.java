package com.qkwl.common.dto.coin;

import java.math.BigDecimal;
import java.util.Date;

public class USDTCollect {
    private Integer id;

    private Boolean isrechargebtc;

    private String address;

    private Integer status;

    private Date createtime;
    
    private Date updatetime;
    
    private BigDecimal rechargebtc;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsrechargebtc() {
        return isrechargebtc;
    }

    public void setIsrechargebtc(Boolean isrechargebtc) {
        this.isrechargebtc = isrechargebtc;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

	public BigDecimal getRechargebtc() {
		return rechargebtc;
	}

	public void setRechargebtc(BigDecimal rechargebtc) {
		this.rechargebtc = rechargebtc;
	}

	public Date getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}
	
    
}