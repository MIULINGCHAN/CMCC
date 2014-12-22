package com.ocs.bean.packagerule;

import com.sleepycat.persist.model.Persistent;

/**
 * 语音业务计费规则类
 * @author Wangchao
 *
 */
@Persistent
public class VoiceServiceRule {
	
	/** 语音业务规则计费资源 */
	private VoiceServiceBillingResource voiceServiceBillingResource;
	
	/** 语音业务批价规则 */
	private PricingRule voiceServicePricingRule;
}
