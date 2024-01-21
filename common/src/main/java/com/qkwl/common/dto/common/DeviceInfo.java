package com.qkwl.common.dto.common;

public class DeviceInfo {
	/*平台：
	WebUser(1, "WEB用户"), 
	APPUser(2, "APP用户"), 
	API(3, "API接口"),
	WebQQ(4, "WEB-QQ"),
	APPQQ(5, "APP-QQ"),
	WebWX(6, "WEB-WX"),
	APPWX(7, "APP-WX");
	**/
	private int platform;
	//设备id
	private String deviceId;
	//APP当前版本
	private String versionCode;
	//app当前build号
	private String buildCode;
	//系统版本：
	private String sysVer;
	//设备机型
	private String deviceModel;
	public int getPlatform() {
		return platform;
	}
	public void setPlatform(int platform) {
		this.platform = platform;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getVersionCode() {
		return versionCode;
	}
	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}
	public String getBuildCode() {
		return buildCode;
	}
	public void setBuildCode(String buildCode) {
		this.buildCode = buildCode;
	}
	public String getSysVer() {
		return sysVer;
	}
	public void setSysVer(String sysVer) {
		this.sysVer = sysVer;
	}
	public String getDeviceModel() {
		return deviceModel;
	}
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}
}
