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
 * @date 2014年12月11日
 */
public class PropertiesUtils {
	
	private static String filePath = "/conf/CMCC_OCS.properties";
	
	/**
	 * 从properties文件中读取key对应的value
	 * @param filePath
	 * @param key
	 * @return key对应的返回值
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
	 * 从配置文件中获取ABM的地址
	 * @return
	 */
	public static String getABMServerIP(){
		return getPropertyValue(filePath,"ABMServerIP");
	}
	
	/**
	 * 从配置文件中获取ABM的端口号
	 * @return
	 */
	public static int getABMServerPort(){
		return Integer.parseInt(getPropertyValue(filePath, "abmPort"));
	}
	
	/**
	 * 从配置文件中获取数据库是否本机访问
	 * @return
	 */
	private static boolean getABMLocalDatabaseAccess(){
		return Boolean.parseBoolean(getPropertyValue(filePath, "databaseLocalAccess"));
	}
	
	/**
	 * 从配置文件中获取数据库地址
	 * 当 数据库是从本机访问，返回本机访问的ip地址
	 * 当数据库是从外机访问，返回外机访问的ip地址
	 * @return
	 */
	public static String getDBIP(){
		if(getABMLocalDatabaseAccess())
			return getPropertyValue(filePath, "databaseIP_DBLA");
		else
			return getPropertyValue(filePath, "databaseIP_DBOA");
	}
	
	/**
	 * 从配置文件中获取数据库用户名
	 * 当 数据库是从本机访问，返回本机访问的用户名
	 * 当数据库是从外机访问，返回外机访问的用户名
	 * @return
	 */
	public static String getDBUsername(){
		if(getABMLocalDatabaseAccess())
			return getPropertyValue(filePath, "userName_DBLA");
		else
			return getPropertyValue(filePath, "userName_DBOA");
	}
	
	/**
	 * 从配置文件中获取数据库用户密码
	 * 当 数据库是从本机访问，返回本机访问的用户密码
	 * 当数据库是从外机访问，返回外机访问的用户密码
	 * @return
	 */
	public static String getDBPassword(){
		if(getABMLocalDatabaseAccess())
			return getPropertyValue(filePath, "userPasswd_DBLA");
		else
			return getPropertyValue(filePath, "userPasswd_DBOA");
	}
	
	/**
	 * 从配置文件中获取OCS的ip
	 * @return
	 */
	public static String getOCSServerIP(){
		return getPropertyValue(filePath,"OCSServerIP");
	}
	
	/**
	 * 从配置文件中获取OCS的端口号
	 * @return
	 */
	public static int getOCSServerPort(){
		return  Integer.parseInt(getPropertyValue(filePath,"ocsPort"));
	}
	
	public static String getLocationFilePath(){
		return getPropertyValue(filePath, "locationProfilePath");
	}
	
	/**
	 * 从配置文件中获取GGSN的ip
	 * @return
	 */
	public static String getGGSNServerIP(){
		return getPropertyValue(filePath,"GGSNServerIP");
	}
	
	/**
	 * 从配置文件中获取Tair Config Server的ip
	 * @return
	 */
	public static String getTairConfigServerIP(){
		return getPropertyValue(filePath, "TairConfigServerIP");
	}
	
	/**
	 * 从配置文件中获取Tair Config Server的端口号
	 * @return
	 */
	public static int getTairConfigServerPort(){
		return Integer.parseInt(getPropertyValue(filePath, "TairConfigServerPort"));
	}

	/**
	 * 从配置文件中获得CF线程池的最大值
	 * @return
	 */
	public static int getCFThreadPoolMaxNum(){
		return Integer.parseInt(getPropertyValue(filePath, "CFThreadPoolMax"));
	}
	
	/**
	 * 从配置文件中获得RF线程池的最大值
	 * @return
	 */
	public static int getRFThreadPoolMaxNum(){
		return Integer.parseInt(getPropertyValue(filePath, "RFThreadPoolMax"));
	}
	
	/**
	 * 从配置文件中获得drl文件地址
	 * @return
	 */
	public static String getDrlFilePath(){
		return getPropertyValue(filePath, "drlFilePath");
	}
	
	/**
	 * 从配置文件中获得drl文件名
	 * @return
	 */
	public static String getDrlFileName(){
		return getPropertyValue(filePath, "drlFileName");
	}
	
	/**
	 * 从配置文件中获得BDB路径
	 * @return
	 */
	public static String getBDBFilePath(){
		return getPropertyValue(filePath, "BDBFilePath");
	}
}
