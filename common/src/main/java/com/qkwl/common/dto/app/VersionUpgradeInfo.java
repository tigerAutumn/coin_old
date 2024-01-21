package com.qkwl.common.dto.app;

/**
 * 版本升级信息
 */
public class VersionUpgradeInfo {
    private String downloadUrl;//	安装包下载地址	string	@mock=https://cdn-h9-img.thy360.com/file/dev/other/20576d16-6bf4-4817-8813-3bb5e9e661cf
    private String updateContent;//t	更新内容	string	@mock=123121
    private int updateType;//e	升级策略 1:不提示升级,2:建议升级,3:强制升级	number	@mock=3
    private String updateVersion;//	可以升级的版本	string	@mock=1
    private String updateVersionNumber;//	可以升级的版本号	number	@mock=2
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	public String getUpdateContent() {
		return updateContent;
	}
	public void setUpdateContent(String updateContent) {
		this.updateContent = updateContent;
	}
	public int getUpdateType() {
		return updateType;
	}
	public void setUpdateType(int updateType) {
		this.updateType = updateType;
	}
	public String getUpdateVersion() {
		return updateVersion;
	}
	public void setUpdateVersion(String updateVersion) {
		this.updateVersion = updateVersion;
	}
	public String getUpdateVersionNumber() {
		return updateVersionNumber;
	}
	public void setUpdateVersionNumber(String updateVersionNumber) {
		this.updateVersionNumber = updateVersionNumber;
	}

    
}
