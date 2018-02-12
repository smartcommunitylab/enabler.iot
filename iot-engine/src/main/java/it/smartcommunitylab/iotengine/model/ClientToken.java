package it.smartcommunitylab.iotengine.model;

public class ClientToken {
	private String token;
	private String appId;
	
	public ClientToken() {}
	
	public ClientToken(String token, String appId) {
		this.token = token;
		this.appId = appId;
	}
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
}
