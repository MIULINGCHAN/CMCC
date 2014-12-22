package com.ocs.dao.impl;

import com.ocs.bean.packagerule.Package;
import com.ocs.bean.packagerule.Package.ServiceType;
import com.ocs.dao.PackageDAO;

public class TestBDB {
    public static final void main(String[] args){
    	
//    	System.out.println(Class.class.getClass().getResource("/").getPath());
//    	System.out.println(System.getProperty("user.dir"));
    	
//    	// ��������
//    	setupPackageData();
    	
    	// ���ʷ�
    	queryTariff("DG3G19", "Rule_DG3G19_BASE");
    	queryTariff("DG3G19", "Rule_DG3G19_Y_SNX");
    	queryTariff("DJ10", "Rule_DJ10_SN");
    	
    }

    private static final void setupPackageData(){
    	BDBEnv env = new BDBEnv();
    	env.setup(false);
    	PackageDAO dao = new PackageDAOBDBImpl(env);
    	
    	// ���� ���еش�3G���Ŀ�19Ԫ�ײ�
    	Package pkg = TestPackageDataGenerator.generateDG3G19();
    	dao.putPackage(pkg);
    	
    	// ���� 10Ԫ���Ӱ��ײ�
    	pkg = TestPackageDataGenerator.generateDJ10();
    	dao.putPackage(pkg);
    	
    	env.close();
    }
    
    private static final void queryTariff(String pkgID,String ruleID){
       	BDBEnv env = new BDBEnv();
    	env.setup(false);
    	PackageDAO dao = new PackageDAOBDBImpl(env);
    	
//    	System.out.println(dao.getPackage(pkg.getPackageID()));
    	
    	System.out.println(pkgID + "," + ruleID + ":" + dao.queryTariff(pkgID, ServiceType.DATA_TRAFFIC, ruleID));
    	
    	env.close();
    }
}
