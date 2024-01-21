package com.qkwl.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {
	private String ethTopic;
	private String gsetTopic;
	private String btcTopic;
	private String mktTopic;
	private String usdtTopic;
	public String getUsdtTopic() {
		return usdtTopic;
	}
	public void setUsdtTopic(String usdtTopic) {
		this.usdtTopic = usdtTopic;
	}
	public String getEthTopic() {
		return ethTopic;
	}
	public void setEthTopic(String ethTopic) {
		this.ethTopic = ethTopic;
	}
	public String getGsetTopic() {
		return gsetTopic;
	}
	public void setGsetTopic(String gsetTopic) {
		this.gsetTopic = gsetTopic;
	}
	public String getBtcTopic() {
		return btcTopic;
	}
	public void setBtcTopic(String btcTopic) {
		this.btcTopic = btcTopic;
	}
	public String getMktTopic() {
		return mktTopic;
	}
	public void setMktTopic(String mktTopic) {
		this.mktTopic = mktTopic;
	}
}
