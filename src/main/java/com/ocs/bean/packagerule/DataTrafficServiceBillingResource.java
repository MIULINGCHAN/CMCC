package com.ocs.bean.packagerule;

import java.util.List;

import com.sleepycat.persist.model.Persistent;

/**
 * ��������ҵ��Ʒѹ���Ʒ���Դ����
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
