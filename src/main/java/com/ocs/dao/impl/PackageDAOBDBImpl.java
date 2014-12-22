package com.ocs.dao.impl;

import java.util.List;

import com.ocs.bean.packagerule.DataTrafficService;
import com.ocs.bean.packagerule.DataTrafficServiceRule;
import com.ocs.bean.packagerule.Package;
import com.ocs.bean.packagerule.Package.ServiceType;
import com.ocs.bean.packagerule.VoiceService;
import com.ocs.dao.PackageDAO;
import com.sleepycat.persist.PrimaryIndex;

public class PackageDAOBDBImpl implements PackageDAO {
	
    // Package Accessors
    PrimaryIndex<String, Package> packages;
    
    public PackageDAOBDBImpl(BDBEnv env) {
    	packages = env.getEntityStore().getPrimaryIndex(String.class, Package.class);
    }

	@Override
	public void putPackage(Package pkg) {
		packages.put(pkg);
	}

	@Override
	public Package getPackage(String pkgID) {
		
		return packages.get(pkgID);
	}

	@Override
	public double queryTariff(String pkgID, ServiceType serviceType,
			String ruleID) {
		Package pkg = getPackage(pkgID);
		
		if ( pkg == null ) 
			return -1;
		
		if ( serviceType == ServiceType.DATA_TRAFFIC ) {
			DataTrafficService service = (DataTrafficService) pkg.getDataTrafficService();
			List<DataTrafficServiceRule> dataTrafficServiceRules = service.getDataTrafficServiceRules();
			for (DataTrafficServiceRule oneRule : dataTrafficServiceRules ) {
				if (oneRule.getRuleID().equals(ruleID) )
					return oneRule.getDataTrafficServicePricingRule().getPrice();
			}
		}
		if ( serviceType == ServiceType.VOICE ) {
			VoiceService service = (VoiceService) pkg.getVoiceService();
			}
		
		return -1;
	}
	
	

}
