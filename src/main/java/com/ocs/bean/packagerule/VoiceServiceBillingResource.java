package com.ocs.bean.packagerule;

import com.sleepycat.persist.model.Persistent;

/**
 * ����ҵ��Ʒ���Դ�࣬������ҵ��Ʒѹ������ɲ���
 * @author Wangchao
 *
 */
@Persistent
public class VoiceServiceBillingResource {
	
	/** �Ʒѷ�ͨ���� */
	private String callerPosition;
	
	/** �������� */
	private String chanel;
	
	/** ͨ���Է���������� */
	private String calleeAttribution;
	
	/** ͨ���Է���������*/
	private String calleeNumType;

}
