package com.ocs.bean.packagerule;

import com.sleepycat.persist.model.Persistent;

/**
 * 数据流量业务计费规则类
 * @author Wangchao
 *
 */
@Persistent
public class DataTrafficServiceRule {
	
	private String ruleName;
	private String ruleID;
	
	/** 数据流量业务计费资源 */
	private DataTrafficServiceBillingResource dataTrafficServiceBillingResource;
	
	/** 数据流量业务批价规则 */
	private PricingRule dataTrafficServicePricingRule;
	
	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getRuleID() {
		return ruleID;
	}

	public void setRuleID(String ruleID) {
		this.ruleID = ruleID;
	}

	public DataTrafficServiceBillingResource getDataTrafficServiceBillingResource() {
		return dataTrafficServiceBillingResource;
	}

	public void setDataTrafficServiceBillingResource(
			DataTrafficServiceBillingResource dataTrafficServiceBillingResource) {
		this.dataTrafficServiceBillingResource = dataTrafficServiceBillingResource;
	}

	public PricingRule getDataTrafficServicePricingRule() {
		return dataTrafficServicePricingRule;
	}

	public void setDataTrafficServicePricingRule(
			PricingRule dataTrafficServicePricingRule) {
		this.dataTrafficServicePricingRule = dataTrafficServicePricingRule;
	}

	@Override
	public String toString() {
		return "DataTrafficServiceRule [ruleName=" + ruleName + ", ruleID="
				+ ruleID + ", dataTrafficServiceBillingResource="
				+ dataTrafficServiceBillingResource
				+ ", dataTrafficServicePricingRule="
				+ dataTrafficServicePricingRule + "]";
	}
	
}
