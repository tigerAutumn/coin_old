package com.qkwl.common.dto.user;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户身份验证
 * @author ZKF
 */
public class FUserIdentity implements Serializable{
	private static final long serialVersionUID = 1L;

	private Integer fid;

    private Integer fuid;

    private String fcountry;

    private String fname;

    private String fcode;

    private Integer ftype;

    private Integer fstatus;

    private Date fcreatetime;

    private Date fupdatetime;
    
    private String fstatus_s;
    
    private String ftype_s;

    private String ip;
    
    private boolean isNewValidate;

    private String imageBest;
    
    private String imageIdcardBack;
    
    private String imageIdcardFront;

    private String bizId;
    
    public String getBizId() {
		return bizId;
	}

	public void setBizId(String bizId) {
		this.bizId = bizId;
	}

	public boolean isNewValidate() {
		return isNewValidate;
	}

	public void setNewValidate(boolean isNewValidate) {
		this.isNewValidate = isNewValidate;
	}

	public String getImageBest() {
		return imageBest;
	}

	public void setImageBest(String imageBest) {
		this.imageBest = imageBest;
	}

	public String getImageIdcardBack() {
		return imageIdcardBack;
	}

	public void setImageIdcardBack(String imageIdcardBack) {
		this.imageIdcardBack = imageIdcardBack;
	}

	public String getImageIdcardFront() {
		return imageIdcardFront;
	}

	public void setImageIdcardFront(String imageIdcardFront) {
		this.imageIdcardFront = imageIdcardFront;
	}

	public Integer getFid() {
        return fid;
    }

    public void setFid(Integer fid) {
        this.fid = fid;
    }

    public Integer getFuid() {
        return fuid;
    }

    public void setFuid(Integer fuid) {
        this.fuid = fuid;
    }

    public String getFcountry() {
        return fcountry;
    }

    public void setFcountry(String fcountry) {
        this.fcountry = fcountry == null ? null : fcountry.trim();
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname == null ? null : fname.trim();
    }

    public String getFcode() {
        return fcode;
    }

    public void setFcode(String fcode) {
        this.fcode = fcode == null ? null : fcode.trim();
    }

    public Integer getFtype() {
        return ftype;
    }

    public void setFtype(Integer ftype) {
        this.ftype = ftype;
    }

    public Integer getFstatus() {
        return fstatus;
    }

    public void setFstatus(Integer fstatus) {
        this.fstatus = fstatus;
    }

    public Date getFcreatetime() {
        return fcreatetime;
    }

    public void setFcreatetime(Date fcreatetime) {
        this.fcreatetime = fcreatetime;
    }

    public Date getFupdatetime() {
        return fupdatetime;
    }

    public void setFupdatetime(Date fupdatetime) {
        this.fupdatetime = fupdatetime;
    }

	public String getFstatus_s() {
		return fstatus_s;
	}

	public void setFstatus_s(String fstatus_s) {
		this.fstatus_s = fstatus_s;
	}

	public String getFtype_s() {
		return ftype_s;
	}

	public void setFtype_s(String ftype_s) {
		this.ftype_s = ftype_s;
	}

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}