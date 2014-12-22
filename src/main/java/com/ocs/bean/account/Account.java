package com.ocs.bean.account;

import java.util.ArrayList;

public class Account {
	public String accountID;
	public String phoneNumber;
	public String numberAttribution;
	public PackageInfo mainService;
	public ArrayList<PackageInfo> additionalServices;

	public String getAccountID() {
		return accountID;
	}

	public void setAccountID(String accountID) {
		this.accountID = accountID;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getNumberAttribution() {
		return numberAttribution;
	}

	public void setNumberAttribution(String numberAttribution) {
		this.numberAttribution = numberAttribution;
	}

	public PackageInfo getMainService() {
		return mainService;
	}

	public void setMainService(PackageInfo mainService) {
		this.mainService = mainService;
	}

	public ArrayList<PackageInfo> getAdditionalServices() {
		return additionalServices;
	}

	public void setAdditionalServices(ArrayList<PackageInfo> additionalServices) {
		this.additionalServices = additionalServices;
	}

	@Override
	public String toString() {
		return "Account [accountID=" + accountID + ", phoneNumber="
				+ phoneNumber + ", numberAttribution=" + numberAttribution
				+ ", mainService=" + mainService + ", additionalServices="
				+ additionalServices + "]";
	}

}
