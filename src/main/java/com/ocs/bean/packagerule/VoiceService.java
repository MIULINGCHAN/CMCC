package com.ocs.bean.packagerule;

import java.util.List;

import com.sleepycat.persist.model.Persistent;

/**
 * ����ҵ����
 * @author Wangchao
 *
 */
@Persistent
public class VoiceService extends MobileBusiness{
	
	/** ����ҵ����� */
	private List<VoiceServiceRule> voiceServiceRules;
	
}
