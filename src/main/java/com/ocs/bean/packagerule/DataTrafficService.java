package com.ocs.bean.packagerule;

import java.util.List;

import com.sleepycat.persist.model.Persistent;

/**
 * ��������ҵ����
 * @author Wangchao
 *
 */
@Persistent
public class DataTrafficService extends MobileBusiness {
	
	/** ��������ҵ����� */
	private List<DataTrafficServiceRule> dataTrafficServiceRules;

	public List<DataTrafficServiceRule> getDataTrafficServiceRules() {
		return dataTrafficServiceRules;
	}

	public void setDataTrafficServiceRules(
			List<DataTrafficServiceRule> dataTrafficServiceRules) {
		this.dataTrafficServiceRules = dataTrafficServiceRules;
	}

	@Override
	public String toString() {
		return "DataTrafficService [dataTrafficServiceRules="
				+ dataTrafficServiceRules + "]";
	}
	
}
