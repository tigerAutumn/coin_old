package com.qkwl.common.dto.user;

import com.qkwl.common.dto.common.BaseParamDTO;
import com.qkwl.common.dto.common.DeviceInfo;

import java.io.Serializable;

/**
 * 请求用户信息的参数实体
 * @author ZKF
 */
public class RequestUserInfo extends BaseParamDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	// 登录名
	private String floginname;
	// 登录类型 0 手机 1邮箱
	private Integer type;
	// 登录密码
	private String floginpassword;
	// QQ登录
	private String fqqopenid;
	// 微信登录
	private String funionid;
	// 验证码
	private String code;
	// 劵商ID
	private Integer fagentid;
	// 推荐人ID 弃用,使用推荐码替代！！！
	private Integer introUid;
	// 推荐码
	private String introcode;
	// 区号
	private String fareacode;
	
	private String nickname;

	private DeviceInfo deviceInfo;
	
	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(DeviceInfo deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getFloginname() {
		return floginname;
	}

	public void setFloginname(String floginname) {
		this.floginname = floginname;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getFloginpassword() {
		return floginpassword;
	}

	public void setFloginpassword(String floginpassword) {
		this.floginpassword = floginpassword;
	}

	public String getFqqopenid() {
		return fqqopenid;
	}

	public void setFqqopenid(String fqqopenid) {
		this.fqqopenid = fqqopenid;
	}

	public String getFunionid() {
		return funionid;
	}

	public void setFunionid(String funionid) {
		this.funionid = funionid;
	}

	public Integer getFagentid() {
		return fagentid;
	}

	public void setFagentid(Integer fagentid) {
		this.fagentid = fagentid;
	}

	public Integer getIntroUid() {
		return introUid;
	}

	public void setIntroUid(Integer introUid) {
		this.introUid = introUid;
	}

	public String getIntrocode() {
		return introcode;
	}

	public void setIntrocode(String introcode) {
		this.introcode = introcode;
	}

	public String getFareacode() {
		return fareacode;
	}

	public void setFareacode(String fareacode) {
		this.fareacode = fareacode;
	}

}
