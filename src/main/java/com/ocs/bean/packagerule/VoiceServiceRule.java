package com.ocs.bean.packagerule;

import com.sleepycat.persist.model.Persistent;

/**
 * ����ҵ��Ʒѹ�����
 * @author Wangchao
 *
 */
@Persistent
public class VoiceServiceRule {
	
	/** ����ҵ�����Ʒ���Դ */
	private VoiceServiceBillingResource voiceServiceBillingResource;
	
	/** ����ҵ�����۹��� */
	private PricingRule voiceServicePricingRule;
}
