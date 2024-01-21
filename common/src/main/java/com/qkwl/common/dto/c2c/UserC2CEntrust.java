package com.qkwl.common.dto.c2c;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.qkwl.common.dto.Enum.DataSourceEnum;
import com.qkwl.common.dto.Enum.c2c.UserC2CEntrustStatusEnum;
import com.qkwl.common.dto.Enum.c2c.UserC2CEntrustTypeEnum;
import com.qkwl.common.dto.capital.FUserBankinfoDTO;

public class UserC2CEntrust {
    private Integer id;
    
    private String orderNumber;

    private Integer coinId;
    
    private String coinName;

    //用户绑定银行卡id
    private Integer bankId;

    private Integer userId;
    
    private String userName;

    private Date createTime;

    private BigDecimal amount;
    
    private BigDecimal money;

    //类型：（1，充值 2，提现）
    private Integer type;
    
    private String typeString;

    private Integer status;
    
    private String statusString;

    private String remark;

    //开户行
    private String bank;

    //开户名
    private String bankAccount;
    
    //银行卡号
    private String bankCode;
    
    //开户行地址
    private String bankAddress;

    private String phone;

    private Date updateTime;

    private Integer adminId;
    
    private String adminName;

    private Integer version;

    private Integer platform;
    
    private String platformString;

    private String startTime;
    
    private String endTime;
    
    //商户id
    private Integer businessId;
    
    //商户名
    private String businessName;
    
    // 交易密码
    private String tradePass;
    
    private String ip;
    
    private BigDecimal price;
    
    //用于资产平衡统计
    private BigDecimal recharge;
    private BigDecimal withdraw;
    private BigDecimal withdrawFrozen;
    
    //状态list
    private List<Integer> statusList;

    public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public List<Integer> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<Integer> statusList) {
		this.statusList = statusList;
	}

	public String getTradePass() {
		return tradePass;
	}

	public void setTradePass(String tradePass) {
		this.tradePass = tradePass;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getBankAddress() {
		return bankAddress;
	}

	public void setBankAddress(String bankAddress) {
		this.bankAddress = bankAddress;
	}
    
    public Integer getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Integer businessId) {
		this.businessId = businessId;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCoinId() {
        return coinId;
    }

    public void setCoinId(Integer coinId) {
        this.coinId = coinId;
    }

    public Integer getBankId() {
        return bankId;
    }

    public void setBankId(Integer bankId) {
        this.bankId = bankId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank == null ? null : bank.trim();
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount == null ? null : bankAccount.trim();
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode == null ? null : bankCode.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public BigDecimal getMoney() {
		return money;
	}

	public void setMoney(BigDecimal money) {
		this.money = money;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getTypeString() {
		return UserC2CEntrustTypeEnum.getValueByCode(type);
	}

	public void setTypeString(String typeString) {
		this.typeString = typeString;
	}

	public String getStatusString() {
		return UserC2CEntrustStatusEnum.getValueByCode(status);
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	public String getPlatformString() {
		return DataSourceEnum.getValueByCode(platform);
	}

	public void setPlatformString(String platformString) {
		this.platformString = platformString;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public BigDecimal getRecharge() {
		if(recharge == null) {
			return BigDecimal.ZERO;
		}
		return recharge;
	}

	public void setRecharge(BigDecimal recharge) {
		this.recharge = recharge;
	}

	public BigDecimal getWithdraw() {
		if(withdraw == null) {
			return BigDecimal.ZERO;
		}
		return withdraw;
	}

	public void setWithdraw(BigDecimal withdraw) {
		this.withdraw = withdraw;
	}

	public BigDecimal getWithdrawFrozen() {
		if(withdrawFrozen == null) {
			return BigDecimal.ZERO;
		}
		return withdrawFrozen;
	}

	public void setWithdrawFrozen(BigDecimal withdrawFrozen) {
		this.withdrawFrozen = withdrawFrozen;
	}

	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	public String getCoinName() {
		return coinName;
	}

	public void setCoinName(String coinName) {
		this.coinName = coinName;
	}

	
	
    
}