/**
 * 
 */
package com.ocs.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.sun.org.apache.bcel.internal.generic.RETURN;


/**
 * @author MiuChan
 *
 * @date 2014��12��11��
 */
public class PropertiesUtils {
	
	private static String filePath = "/conf/CMCC_OCS.properties";
	
	/**
	 * ��properties�ļ��ж�ȡkey��Ӧ��value
	 * @param filePath
	 * @param key
	 * @return key��Ӧ�ķ���ֵ
	 */
	private static String getPropertyValue(String filePath, String key){
		Properties property = new Properties();
		try {
			property.load(new FileInputStream(System.getProperty("user.dir")+filePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String value = property.getProperty(key);
		return value;
	}
	
	/**
	 * �������ļ��л�ȡABM�ĵ�ַ
	 * @return
	 */
	public static String getABMServerIP(){
		return getPropertyValue(filePath,"ABMServerIP");
	}
	
	/**
	 * �������ļ��л�ȡABM�Ķ˿ں�
	 * @return
	 */
	public static int getABMServerPort(){
		return Integer.parseInt(getPropertyValue(filePath, "abmPort"));
	}
	
	/**
	 * �������ļ��л�ȡ���ݿ��Ƿ񱾻�����
	 * @return
	 */
	private static boolean getABMLocalDatabaseAccess(){
		return Boolean.parseBoolean(getPropertyValue(filePath, "databaseLocalAccess"));
	}
	
	/**
	 * �������ļ��л�ȡ���ݿ��ַ
	 * �� ���ݿ��Ǵӱ������ʣ����ر������ʵ�ip��ַ
	 * �����ݿ��Ǵ�������ʣ�����������ʵ�ip��ַ
	 * @return
	 */
	public static String getDBIP(){
		if(getABMLocalDatabaseAccess())
			return getPropertyValue(filePath, "databaseIP_DBLA");
		else
			return getPropertyValue(filePath, "databaseIP_DBOA");
	}
	
	/**
	 * �������ļ��л�ȡ���ݿ��û���
	 * �� ���ݿ��Ǵӱ������ʣ����ر������ʵ��û���
	 * �����ݿ��Ǵ�������ʣ�����������ʵ��û���
	 * @return
	 */
	public static String getDBUsername(){
		if(getABMLocalDatabaseAccess())
			return getPropertyValue(filePath, "userName_DBLA");
		else
			return getPropertyValue(filePath, "userName_DBOA");
	}
	
	/**
	 * �������ļ��л�ȡ���ݿ��û�����
	 * �� ���ݿ��Ǵӱ������ʣ����ر������ʵ��û�����
	 * �����ݿ��Ǵ�������ʣ�����������ʵ��û�����
	 * @return
	 */
	public static String getDBPassword(){
		if(getABMLocalDatabaseAccess())
			return getPropertyValue(filePath, "userPasswd_DBLA");
		else
			return getPropertyValue(filePath, "userPasswd_DBOA");
	}
	
	/**
	 * �������ļ��л�ȡOCS��ip
	 * @return
	 */
	public static String getOCSServerIP(){
		return getPropertyValue(filePath,"OCSServerIP");
	}
	
	/**
	 * �������ļ��л�ȡOCS�Ķ˿ں�
	 * @return
	 */
	public static int getOCSServerPort(){
		return  Integer.parseInt(getPropertyValue(filePath,"ocsPort"));
	}
	
	public static String getLocationFilePath(){
		return getPropertyValue(filePath, "locationProfilePath");
	}
	
	/**
	 * �������ļ��л�ȡGGSN��ip
	 * @return
	 */
	public static String getGGSNServerIP(){
		return getPropertyValue(filePath,"GGSNServerIP");
	}
	
	/**
	 * �������ļ��л�ȡTair Config Server��ip
	 * @return
	 */
	public static String getTairConfigServerIP(){
		return getPropertyValue(filePath, "TairConfigServerIP");
	}
	
	/**
	 * �������ļ��л�ȡTair Config Server�Ķ˿ں�
	 * @return
	 */
	public static int getTairConfigServerPort(){
		return Integer.parseInt(getPropertyValue(filePath, "TairConfigServerPort"));
	}

	/**
	 * �������ļ��л��CF�̳߳ص����ֵ
	 * @return
	 */
	public static int getCFThreadPoolMaxNum(){
		return Integer.parseInt(getPropertyValue(filePath, "CFThreadPoolMax"));
	}
	
	/**
	 * �������ļ��л��RF�̳߳ص����ֵ
	 * @return
	 */
	public static int getRFThreadPoolMaxNum(){
		return Integer.parseInt(getPropertyValue(filePath, "RFThreadPoolMax"));
	}
	
	/**
	 * �������ļ��л��drl�ļ���ַ
	 * @return
	 */
	public static String getDrlFilePath(){
		return getPropertyValue(filePath, "drlFilePath");
	}
	
	/**
	 * �������ļ��л��drl�ļ���
	 * @return
	 */
	public static String getDrlFileName(){
		return getPropertyValue(filePath, "drlFileName");
	}
	
	/**
	 * �������ļ��л��BDB·��
	 * @return
	 */
	public static String getBDBFilePath(){
		return getPropertyValue(filePath, "BDBFilePath");
	}
}
