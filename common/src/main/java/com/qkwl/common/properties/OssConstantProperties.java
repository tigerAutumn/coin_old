package com.qkwl.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author weijunbiao
 * @Date 17-4-20
 */
@ConfigurationProperties(prefix = "ossConstant")
public class OssConstantProperties {
	private String bucketBase;
	private String secretKey;
	private String accessKey;
    private String ossEndpoint;
    private String bucketValidate;
    
    public String getBucketValidate() {
		return bucketValidate;
	}
	public void setBucketValidate(String bucketValidate) {
		this.bucketValidate = bucketValidate;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public String getBucketBase() {
		return bucketBase;
	}
	public void setBucketBase(String bucketBase) {
		this.bucketBase = bucketBase;
	}
	public String getAccessKey() {
		return accessKey;
	}
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	public String getOssEndpoint() {
		return ossEndpoint;
	}
	public void setOssEndpoint(String ossEndpoint) {
		this.ossEndpoint = ossEndpoint;
	}
}
