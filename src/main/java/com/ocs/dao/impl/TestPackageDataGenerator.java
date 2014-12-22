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
 * 用于生成测试所用的套餐数据
 * @author Wang Chao
 *
 */
public class TestPackageDataGenerator {
	
	/**
	 * 生成19元3G网聊卡套餐(数据流量部分)
	 */
	public static Package generateDG3G19() {
		Package DG3G19 = new Package();
		
		MobileBusiness dataTrafficService = new DataTrafficService();
		List<DataTrafficServiceRule> dataTrafficServiceRules = new ArrayList<DataTrafficServiceRule>();
		List<String> appChannel = new ArrayList<String>();
		appChannel.add("All");
		//规则一：国内
		DataTrafficServiceRule rule1 = new DataTrafficServiceRule();
		DataTrafficServiceBillingResource billingRes1 = new DataTrafficServiceBillingResource();
		billingRes1.setTrafficGenerationPlace("国内");
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
		
		//规则二：省内
		DataTrafficServiceRule rule2 = new DataTrafficServiceRule();
		DataTrafficServiceBillingResource billingRes2 = new DataTrafficServiceBillingResource();
		billingRes2.setTrafficGenerationPlace("省内");
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
		
		//规则三：省内（闲时）
		DataTrafficServiceRule rule3 = new DataTrafficServiceRule();
		DataTrafficServiceBillingResource billingRes3 = new DataTrafficServiceBillingResource();
		billingRes3.setTrafficGenerationPlace("省内");
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
		
		//规则四：一般规则
		DataTrafficServiceRule rule4 = new DataTrafficServiceRule();
		DataTrafficServiceBillingResource billingRes4 = new DataTrafficServiceBillingResource();
		billingRes4.setTrafficGenerationPlace("国内");
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
		DG3G19.setDescription("19元3G网聊卡套餐");
		DG3G19.setBrand("动感地带");
		DG3G19.setDataTrafficService(dataTrafficService);
		
		return DG3G19;
	}
	
	/**
	 * 生成10元流量叠加包
	 */
	public static Package generateDJ10() {
		Package DJ10 = new Package();
		
		MobileBusiness dataTrafficService = new DataTrafficService();
		List<DataTrafficServiceRule> dataTrafficServiceRules = new ArrayList<DataTrafficServiceRule>();
		List<String> appChannel = new ArrayList<String>();
		appChannel.add("All");
		//规则一：国内
		DataTrafficServiceRule rule1 = new DataTrafficServiceRule();
		DataTrafficServiceBillingResource billingRes1 = new DataTrafficServiceBillingResource();
		billingRes1.setTrafficGenerationPlace("国内");
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
		
		//规则二：省内
		DataTrafficServiceRule rule2 = new DataTrafficServiceRule();
		DataTrafficServiceBillingResource billingRes2 = new DataTrafficServiceBillingResource();
		billingRes2.setTrafficGenerationPlace("省内");
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
		DJ10.setDescription("10元流量叠加包");
		DJ10.setBrand("增值业务");
		DJ10.setDataTrafficService(dataTrafficService);
		
		return DJ10;
	}
}
