package com.ocs.utils;

import java.io.UnsupportedEncodingException;

import com.ocs.protocol.diameter.AVP;
import com.ocs.protocol.diameter.AVP_Grouped;
import com.ocs.protocol.diameter.AVP_OctetString;
import com.ocs.protocol.diameter.AVP_UTF8String;
import com.ocs.protocol.diameter.AVP_Unsigned32;
import com.ocs.protocol.diameter.AVP_Unsigned64;
import com.ocs.protocol.diameter.InvalidAVPLengthException;
import com.ocs.protocol.diameter.Message;
import com.ocs.protocol.diameter.ProtocolConstants;

/**
 * ���ڴ����Ķ���ĸ���������
 * @author Wang Chao
 *
 */
public class MessageUtils {
	
	/**
	 * ���ұ�����Ϣ�е��ֻ�����
	 */
	public static String querySubscriptionID(Message message) {
		String result = null;
		
		AVP avp = message.find(ProtocolConstants.DI_SUBSCRIPTION_ID);
		if ( avp != null ) {
			AVP[] avps = null;
			try {
				avps = new AVP_Grouped(avp).queryAVPs();
			} catch (InvalidAVPLengthException e) {
				e.printStackTrace();
			}
			if ( avps != null && avps.length > 1 ) {
				result = new AVP_UTF8String(avps[1]).queryValue();
			}
		}
		
		return result;
	}
	
	/**
	 * ���ұ�����Ϣ�е��û��˻�id
	 */
	public static String queryAccountId(Message message){
		String result = "";

		AVP avp = message.find(ProtocolConstants._3GPP_DI_ACCOUNT_ID);
		if( avp != null ){
			result = new AVP_UTF8String(avp).queryValue();
		}
		
		return result;
	}
	
	/**
	 * ���ұ�����Ϣ�е��û��ֻ����������
	 */
	public static String querySubscriberBelongArea(Message message){
		String result = null;

		AVP avp = message.find(ProtocolConstants._3GPP_DI_SUBSCRIBER_INFORMATION);
		if( avp != null ){
			AVP[] avps = null;
			try {
				avps = new AVP_Grouped(avp).queryAVPs();
			} catch (InvalidAVPLengthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(AVP tavp : avps ){
				if(tavp.code == ProtocolConstants._3GPP_DI_SUBSCRIBER_BELONG_AREA_NO){
					result = new AVP_UTF8String(tavp).queryValue();
				}
			}
		}
		
		return result;
	}
	
	/**
	 * ���ұ�����Ϣ�е�session id
	 */
	public static String querySessionID(Message message) {
		String result = null;
		
		AVP avp = message.find(ProtocolConstants.DI_SESSION_ID);
		if ( avp != null ) {
			result = new AVP_UTF8String(avp).queryValue();
		}
		
		return result;
	}
	
	/**
	 * �����ն��û�����λ��
	 */
	public static String queryUserLocation(Message message) {
		String result = null;
		
		AVP avp1 = message.find(ProtocolConstants._3GPP_SERVICE_INFORMATION);
		if ( avp1 != null ) {
			AVP[] avps1 = null;
			try {
				avps1 = new AVP_Grouped(avp1).queryAVPs();
			} catch (InvalidAVPLengthException e) {
				e.printStackTrace();
			}
			
			AVP avp2 = null;
			if ( avps1 != null ) {
				avp2 = avps1[0];
			}
			AVP[] avps2 = null;
			try {
				avps2 = new  AVP_Grouped(avp2).queryAVPs();
			} catch (InvalidAVPLengthException e) {
				e.printStackTrace();
			}
			if ( avps2 != null ) {
				result = byteArray2String(new AVP_OctetString(avps2[0]).queryValue());
			}
		}
		
		return result;
	}
	
	/**
	 * ����U����Ԥ������Ԥ��ʹ��������Ϣ
	 */
	public static long queryRequestedServiceUnit(Message message) {
		long result = -1;
		
		AVP avp1 = message.find(ProtocolConstants.DI_MULTIPLE_SERVICES_CREDIT_CONTROL);
		if ( avp1 != null ) {
			AVP[] avps1 = null;
			try {
				avps1 = new AVP_Grouped(avp1).queryAVPs();
			} catch (InvalidAVPLengthException e) {
				e.printStackTrace();
			}
			
			AVP avp2 = null;
			if ( avps1 != null ) {
				avp2 = avps1[0];
			}
			AVP[] avps2 = null;
			try {
				avps2 = new  AVP_Grouped(avp2).queryAVPs();
			} catch (InvalidAVPLengthException e) {
				e.printStackTrace();
			}
			if ( avps2 != null ) {
				try {
					result = new AVP_Unsigned64(avps2[0]).queryValue();
				} catch (InvalidAVPLengthException e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
	
	/**
	 * ����U/T����ʵ��ʹ�õ������Ϣ
	 */
	public static long queryUsedServiceUnit(Message message) {
		long result = -1;
		
		AVP avp1 = message.find(ProtocolConstants.DI_MULTIPLE_SERVICES_CREDIT_CONTROL);
		if ( avp1 != null ) {
			AVP[] avps1 = null;
			try {
				avps1 = new AVP_Grouped(avp1).queryAVPs();
			} catch (InvalidAVPLengthException e) {
				e.printStackTrace();
			}
			
			AVP avp2 = null;
			if ( avps1 != null ) {
				avp2 = avps1[0];
			}
			AVP[] avps2 = null;
			try {
				avps2 = new  AVP_Grouped(avp2).queryAVPs();
			} catch (InvalidAVPLengthException e) {
				e.printStackTrace();
			}
			if ( avps2 != null ) {
				try {
					result = new AVP_Unsigned64(avps2[0]).queryValue();
				} catch (InvalidAVPLengthException e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
	
	/**
	 * ��ѯ���������Ϣ
	 */
	public static long queryGrantedServiceUnit(Message message) {
		long result = -1;
		
		AVP avp = message.find(ProtocolConstants.DI_GRANTED_SERVICE_UNIT);
		if ( avp != null ) {
			AVP[] avps = null;
			try {
				avps = new AVP_Grouped(avp).queryAVPs();
			} catch (InvalidAVPLengthException e) {
				e.printStackTrace();
			}
			if ( avps != null ) {
				try {
					result = new AVP_Unsigned64(avps[0]).queryValue();
				} catch (InvalidAVPLengthException e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
	
	/**
	 * ��ѯCC_REQUEST_NUMBER
	 */
	public static int queryCCReqeustNumber(Message message) {
		int result = -1;
		
		AVP avp = message.find(ProtocolConstants.DI_CC_REQUEST_NUMBER);
		if ( avp != null ) {
			try {
				result = new AVP_Unsigned32(avp).queryValue();
			} catch (InvalidAVPLengthException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	/**
	 * ���ַ���ת��Ϊbyte[]
	 */
	public static byte[] string2ByteArray(String str) {
		byte[] result = null;
		
    	try {
    		result = str.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
		return result;
	}
	
	/**
	 * ��byte[]ת��Ϊ�ַ���
	 */
	public static String byteArray2String(byte[] arr) {
		String result = null;
		
		try {
			result = new String(arr,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
