package it.smartcommunitylab.iotengine.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DataSetConf extends BaseObject {
	private String domain;
	private String dataset;
	private String user;
	private String secret;
	private String userId;
	private String token;
	private List<String> devices = new ArrayList<String>();

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public List<String> getDevices() {
		return devices;
	}

	public void setDevices(List<String> devices) {
		this.devices = devices;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return domain + "/" + dataset + "/" + getId();
	}

}
