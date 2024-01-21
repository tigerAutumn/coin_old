package com.qkwl.service.mqtt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {
	private int port;
	private String url;
	private String acessKey;
	private String secretKey;
	private String clientId;
	private String ethTopic;
	private String gsetTopic;
	private String btcTopic;
	private String usdtTopic;
	private String mktTopic;
	private String webEthTopic;
	private String webKlineTopic;
	private String webGsetTopic;
	private String webBtcTopic;
	private String webMktTopic;
	private String webRealTimeTrade;
	private String webUsdtTopic;
	
	public String getUsdtTopic() {
		return usdtTopic;
	}
	public void setUsdtTopic(String usdtTopic) {
		this.usdtTopic = usdtTopic;
	}
	public String getWebUsdtTopic() {
		return webUsdtTopic;
	}
	public void setWebUsdtTopic(String webUsdtTopic) {
		this.webUsdtTopic = webUsdtTopic;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
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
	public String getWebEthTopic() {
		return webEthTopic;
	}
	public void setWebEthTopic(String webEthTopic) {
		this.webEthTopic = webEthTopic;
	}
	public String getWebKlineTopic() {
		return webKlineTopic;
	}
	public void setWebKlineTopic(String webKlineTopic) {
		this.webKlineTopic = webKlineTopic;
	}
	public String getWebGsetTopic() {
		return webGsetTopic;
	}
	public void setWebGsetTopic(String webGsetTopic) {
		this.webGsetTopic = webGsetTopic;
	}
	public String getWebBtcTopic() {
		return webBtcTopic;
	}
	public void setWebBtcTopic(String webBtcTopic) {
		this.webBtcTopic = webBtcTopic;
	}
	public String getWebMktTopic() {
		return webMktTopic;
	}
	public void setWebMktTopic(String webMktTopic) {
		this.webMktTopic = webMktTopic;
	}
	public String getWebRealTimeTrade() {
		return webRealTimeTrade;
	}
	public void setWebRealTimeTrade(String webRealTimeTrade) {
		this.webRealTimeTrade = webRealTimeTrade;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAcessKey() {
		return acessKey;
	}
	public void setAcessKey(String acessKey) {
		this.acessKey = acessKey;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	

    
}
