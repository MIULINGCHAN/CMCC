package com.ocs.dao.impl;

import java.util.ArrayList;
import java.util.List;

import com.ocs.bean.packagerule.DataTrafficService;
import com.ocs.bean.packagerule.DataTrafficServiceBillingResource;
import com.ocs.bean.packagerule.DataTrafficServiceRule;
import com.ocs.bean.packagerule.MobileBusiness;
import com.ocs.bean.packagerule.Package;
import com.ocs.bean.packagerule.Package.AdditionalServiceType;
import com.ocs.bean.packagerule.PricingRule;

/**
 * �������ɲ������õ��ײ�����
 * @author Wang Chao
 *
 */
public class TestPackageDataGenerator {
	
	/**
	 * ����19Ԫ3G���Ŀ��ײ�(������������)
	 */
	public static Package generateDG3G19() {
		Package DG3G19 = new Package();
		
		MobileBusiness dataTrafficService = new DataTrafficService();
		List<DataTrafficServiceRule> dataTrafficServiceRules = new ArrayList<DataTrafficServiceRule>();
		List<String> appChannel = new ArrayList<String>();
		appChannel.add("All");
		//����һ������
		DataTrafficServiceRule rule1 = new DataTrafficServiceRule();
		DataTrafficServiceBillingResource billingRes1 = new DataTrafficServiceBillingResource();
		billingRes1.setTrafficGenerationPlace("����");
		billingRes1.setAppChannel(appChannel);
		PricingRule pricingRule1 = new PricingRule();
		pricingRule1.setPeriodStart(0);
		pricingRule1.setPeriodEnd(24);
		pricingRule1.setAccumulationFloor(0);
		pricingRule1.setAccumulationCeiling(200*1024);
		pricingRule1.setPrice(0.00);
		rule1.setDataTrafficServiceBillingResource(billingRes1);
		rule1.setDataTrafficServicePricingRule(pricingRule1);
		rule1.setRuleName("Rule_DG3G19_M_Z");
		rule1.setRuleID("Rule_DG3G19_M_Z");
		dataTrafficServiceRules.add(rule1);
		
		//�������ʡ��
		DataTrafficServiceRule rule2 = new DataTrafficServiceRule();
		DataTrafficServiceBillingResource billingRes2 = new DataTrafficServiceBillingResource();
		billingRes2.setTrafficGenerationPlace("ʡ��");
		billingRes2.setAppChannel(appChannel);
		PricingRule pricingRule2 = new PricingRule();
		pricingRule2.setPeriodStart(0);
		pricingRule2.setPeriodEnd(24);
		pricingRule2.setAccumulationFloor(0);
		pricingRule2.setAccumulationCeiling(500*1024);
		pricingRule2.setPrice(0.00);
		rule2.setDataTrafficServiceBillingResource(billingRes2);
		rule2.setDataTrafficServicePricingRule(pricingRule2);
		rule2.setRuleName("Rule_DG3G19_Y_SN");
		rule2.setRuleID("Rule_DG3G19_Y_SN");
		dataTrafficServiceRules.add(rule2);
		
		//��������ʡ�ڣ���ʱ��
		DataTrafficServiceRule rule3 = new DataTrafficServiceRule();
		DataTrafficServiceBillingResource billingRes3 = new DataTrafficServiceBillingResource();
		billingRes3.setTrafficGenerationPlace("ʡ��");
		billingRes3.setAppChannel(appChannel);
		PricingRule pricingRule3 = new PricingRule();
		pricingRule3.setPeriodStart(0);
		pricingRule3.setPeriodEnd(10);
		pricingRule3.setAccumulationFloor(0);
		pricingRule3.setAccumulationCeiling(1024*1024);
		pricingRule3.setPrice(0.00);
		rule3.setDataTrafficServiceBillingResource(billingRes3);
		rule3.setDataTrafficServicePricingRule(pricingRule3);
		rule3.setRuleName("Rule_DG3G19_Y_SNX");
		rule3.setRuleID("Rule_DG3G19_Y_SNX");
		dataTrafficServiceRules.add(rule3);
		
		//�����ģ�һ�����
		DataTrafficServiceRule rule4 = new DataTrafficServiceRule();
		DataTrafficServiceBillingResource billingRes4 = new DataTrafficServiceBillingResource();
		billingRes4.setTrafficGenerationPlace("����");
		billingRes4.setAppChannel(appChannel);
		PricingRule pricingRule4 = new PricingRule();
		pricingRule4.setPeriodStart(0);
		pricingRule4.setPeriodEnd(24);
		pricingRule4.setAccumulationFloor(0);
		pricingRule4.setAccumulationCeiling(Integer.MAX_VALUE);
		pricingRule4.setPrice(0.5);
		rule4.setDataTrafficServiceBillingResource(billingRes4);
		rule4.setDataTrafficServicePricingRule(pricingRule4);
		rule4.setRuleName("Rule_DG3G19_BASE");
		rule4.setRuleID("Rule_DG3G19_BASE");
		dataTrafficServiceRules.add(rule4);
		
		((DataTrafficService)dataTrafficService).setDataTrafficServiceRules(dataTrafficServiceRules);
		DG3G19.setAdditionalService(false);
		DG3G19.setPackageID("DG3G19");
		DG3G19.setPackageName("DG3G19");
		DG3G19.setDescription("19Ԫ3G���Ŀ��ײ�");
		DG3G19.setBrand("���еش�");
		DG3G19.setDataTrafficService(dataTrafficService);
		
		return DG3G19;
	}
	
	/**
	 * ����10Ԫ�������Ӱ�
	 */
	public static Package generateDJ10() {
		Package DJ10 = new Package();
		
		MobileBusiness dataTrafficService = new DataTrafficService();
		List<DataTrafficServiceRule> dataTrafficServiceRules = new ArrayList<DataTrafficServiceRule>();
		List<String> appChannel = new ArrayList<String>();
		appChannel.add("All");
		//����һ������
		DataTrafficServiceRule rule1 = new DataTrafficServiceRule();
		DataTrafficServiceBillingResource billingRes1 = new DataTrafficServiceBillingResource();
		billingRes1.setTrafficGenerationPlace("����");
		billingRes1.setAppChannel(appChannel);
		PricingRule pricingRule1 = new PricingRule();
		pricingRule1.setPeriodStart(0);
		pricingRule1.setPeriodEnd(24);
		pricingRule1.setAccumulationFloor(0);
		pricingRule1.setAccumulationCeiling(70*1024);
		pricingRule1.setPrice(0.00);
		rule1.setDataTrafficServiceBillingResource(billingRes1);
		rule1.setDataTrafficServicePricingRule(pricingRule1);
		rule1.setRuleName("Rule_DJ10_GN");
		rule1.setRuleID("Rule_DJ10_GN");
		dataTrafficServiceRules.add(rule1);
		
		//�������ʡ��
		DataTrafficServiceRule rule2 = new DataTrafficServiceRule();
		DataTrafficServiceBillingResource billingRes2 = new DataTrafficServiceBillingResource();
		billingRes2.setTrafficGenerationPlace("ʡ��");
		billingRes2.setAppChannel(appChannel);
		PricingRule pricingRule2 = new PricingRule();
		pricingRule2.setPeriodStart(0);
		pricingRule2.setPeriodEnd(24);
		pricingRule2.setAccumulationFloor(0);
		pricingRule2.setAccumulationCeiling(30*1024);
		pricingRule2.setPrice(0.00);
		rule2.setDataTrafficServiceBillingResource(billingRes2);
		rule2.setDataTrafficServicePricingRule(pricingRule2);
		rule2.setRuleName("Rule_DJ10_SN");
		rule2.setRuleID("Rule_DJ10_SN");
		dataTrafficServiceRules.add(rule2);
		
		((DataTrafficService)dataTrafficService).setDataTrafficServiceRules(dataTrafficServiceRules);
		DJ10.setAdditionalService(true);
		DJ10.setAdditionalServiceType(AdditionalServiceType.DATA_TRAFFIC);
		DJ10.setPackageID("DJ10");
		DJ10.setPackageName("DJ10");
		DJ10.setDescription("10Ԫ�������Ӱ�");
		DJ10.setBrand("��ֵҵ��");
		DJ10.setDataTrafficService(dataTrafficService);
		
		return DJ10;
	}
}
