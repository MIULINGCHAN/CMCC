package com.ocs.bean.packagerule;

import com.sleepycat.persist.model.Persistent;

/**
 * 语音业务计费资源类，是语音业务计费规则的组成部分
 * @author Wangchao
 *
 */
@Persistent
public class VoiceServiceBillingResource {
	
	/** 计费方通话地 */
	private String callerPosition;
	
	/** 呼叫渠道 */
	private String chanel;
	
	/** 通话对方号码归属地 */
	private String calleeAttribution;
	
	/** 通话对方号码类型*/
	private String calleeNumType;

}
