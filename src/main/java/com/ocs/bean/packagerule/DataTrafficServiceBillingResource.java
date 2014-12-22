package com.ocs.bean.packagerule;

import java.util.List;

import com.sleepycat.persist.model.Persistent;

/**
 * 数据流量业务计费规则计费资源部分
 * @author Wang Chao
 *
 */
@Persistent
public class DataTrafficServiceBillingResource {
	
	private String trafficGenerationPlace;
	private List<String> appChannel;
	
	public String getTrafficGenerationPlace() {
		return trafficGenerationPlace;
	}
	public void setTrafficGenerationPlace(String trafficGenerationPlace) {
		this.trafficGenerationPlace = trafficGenerationPlace;
	}
	public List<String> getAppChannel() {
		return appChannel;
	}
	public void setAppChannel(List<String> appChannel) {
		this.appChannel = appChannel;
	}
	
	@Override
	public String toString() {
		return "DataTrafficServiceBillingResource [trafficGenerationPlace="
				+ trafficGenerationPlace + ", appChannel=" + appChannel + "]";
	}
	
}
