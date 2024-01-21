package com.qkwl.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "faceid")
public class FaceIdProperties {
	private String token_url;
	private String result_url;
	private String api_key;
	private String api_secret;
	private String return_url;
	private String notify_url;
	private String comparison_type;
	private String idcard_mode;
	private String web_title;
	public String getWeb_title() {
		return web_title;
	}
	public void setWeb_title(String web_title) {
		this.web_title = web_title;
	}
	public String getToken_url() {
		return token_url;
	}
	public void setToken_url(String token_url) {
		this.token_url = token_url;
	}
	public String getResult_url() {
		return result_url;
	}
	public void setResult_url(String result_url) {
		this.result_url = result_url;
	}
	public String getApi_key() {
		return api_key;
	}
	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}
	public String getApi_secret() {
		return api_secret;
	}
	public void setApi_secret(String api_secret) {
		this.api_secret = api_secret;
	}
	public String getReturn_url() {
		return return_url;
	}
	public void setReturn_url(String return_url) {
		this.return_url = return_url;
	}
	public String getNotify_url() {
		return notify_url;
	}
	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}
	public String getComparison_type() {
		return comparison_type;
	}
	public void setComparison_type(String comparison_type) {
		this.comparison_type = comparison_type;
	}
	public String getIdcard_mode() {
		return idcard_mode;
	}
	public void setIdcard_mode(String idcard_mode) {
		this.idcard_mode = idcard_mode;
	}
}
