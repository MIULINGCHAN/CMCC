package com.ocs.dao;

import com.ocs.bean.packagerule.Package;
import com.ocs.bean.packagerule.Package.ServiceType;

/**
 * 定义对套餐进行存取的接口
 * @author Wang Chao
 *
 */
public interface PackageDAO {
	
	public void putPackage(Package pkg);
	
	public Package getPackage(String pkgID);
	
	public double queryTariff(String pkgID, ServiceType serviceType, String ruleID);
}
