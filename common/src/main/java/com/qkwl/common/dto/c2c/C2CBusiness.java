package com.qkwl.common.dto.c2c;

import java.math.BigDecimal;

import com.qkwl.common.dto.Enum.c2c.C2CBusinessStatusEnum;
import com.qkwl.common.dto.Enum.c2c.C2CBusinessTypeEnum;

public class C2CBusiness {
    private Integer id;

    private String businessName;

    private Integer type;
    
    private String typeString;

    private String bankName;

    private String bankAddress;

    private String bankNumber;

    private Integer orderCount;

    private BigDecimal coinCount;

    private String orderTime;
    
    private String phone;

    private Integer sortId;
    
    private Integer status;
    
    private String statusString;
    
    private String bankAccountName;
    
    private Integer coinId;
    //币种名称
    private String shortName;
    
    public Integer getCoinId() {
		return coinId;
	}

	public void setCoinId(Integer coinId) {
		this.coinId = coinId;
	}

	public Integer getSortId() {
		return sortId;
	}

	public void setSortId(Integer sortId) {
		this.sortId = sortId;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName == null ? null : businessName.trim();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName == null ? null : bankName.trim();
    }

    public String getBankAddress() {
        return bankAddress;
    }

    public void setBankAddress(String bankAddress) {
        this.bankAddress = bankAddress == null ? null : bankAddress.trim();
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public void setBankNumber(String bankNumber) {
        this.bankNumber = bankNumber == null ? null : bankNumber.trim();
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getCoinCount() {
        return coinCount;
    }

    public void setCoinCount(BigDecimal coinCount) {
        this.coinCount = coinCount;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime == null ? null : orderTime.trim();
    }

	public String getTypeString() {
		return C2CBusinessTypeEnum.getValueByCode(type);
	}

	public void setTypeString(String typeString) {
		this.typeString = typeString;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getBankAccountName() {
		return bankAccountName;
	}

	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = bankAccountName;
	}

	public String getStatusString() {
		return C2CBusinessStatusEnum.getValueByCode(status);
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	
}