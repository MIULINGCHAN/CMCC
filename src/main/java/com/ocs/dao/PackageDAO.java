package com.ocs.dao;

import com.ocs.bean.packagerule.Package;
import com.ocs.bean.packagerule.Package.ServiceType;

/**
 * ������ײͽ��д�ȡ�Ľӿ�
 * @author Wang Chao
 *
 */
public interface PackageDAO {
	
	public void putPackage(Package pkg);
	
	public Package getPackage(String pkgID);
	
	public double queryTariff(String pkgID, ServiceType serviceType, String ruleID);
}
