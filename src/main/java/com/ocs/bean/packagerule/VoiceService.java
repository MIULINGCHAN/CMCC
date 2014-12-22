package com.ocs.bean.packagerule;

import java.util.List;

import com.sleepycat.persist.model.Persistent;

/**
 * 语音业务类
 * @author Wangchao
 *
 */
@Persistent
public class VoiceService extends MobileBusiness{
	
	/** 语音业务规则集 */
	private List<VoiceServiceRule> voiceServiceRules;
	
}
