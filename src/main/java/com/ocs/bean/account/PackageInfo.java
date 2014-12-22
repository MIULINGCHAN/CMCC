package com.ocs.bean.account;

import java.util.ArrayList;
import java.util.Date;

public class PackageInfo {
	public String packageID;
	public String packageName;
	public Date validTime_Start;
	public Date validTime_End;
	public ArrayList<RuleUsage> usages;
	
	public String getPn(){
		return packageName;
	}
	
	public String getPrintString(){
		String usagesDetails="";
		for(RuleUsage ru : usages){
			usagesDetails+=("\n\t\t"+ru.toString());
		}
		
		String s = "";
		s = "PackageInfo[\n"+
				"\tpackageID:"+packageID+",\n"+
				"\tpackageName:"+packageName+",\n"+
				"\tvalidTime_Start:"+validTime_Start+",\n"+
				"\tvalidTime_End:"+validTime_End+",\n"+
				"\tusages:["+usagesDetails+"\n\t],\n"+
				"\t]";
		
		return s;
	}
	
	public void printObject(){
		System.out.println(getPrintString());
	}

	@Override
	public String toString() {
		return "PackageInfo [packageID=" + packageID + ", packageName="
				+ packageName + ", validTime_Start=" + validTime_Start
				+ ", validTime_End=" + validTime_End + ", usages=" + usages
				+ "]";
	}
	
	
}
