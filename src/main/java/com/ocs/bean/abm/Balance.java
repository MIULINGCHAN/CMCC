package com.ocs.bean.abm;

import java.sql.Timestamp;

public class Balance {
	private long balanceID;
	private int balanceType;
	private Timestamp balanceExpDate;
	private double balanceValue;
	
	public Balance(){
		this.balanceID = 0;
		this.balanceType = 0;
		this.balanceExpDate = null;
		this.balanceValue = 0;
	}
	
	public Balance(long balanceID, int balanceType, Timestamp balanceExpDate,
			double balanceValue) {
		this.balanceID = balanceID;
		this.balanceType = balanceType;
		this.balanceExpDate = balanceExpDate;
		this.balanceValue = balanceValue;
	}

	@Override
	public String toString() {
		return "Balance [balanceID=" + balanceID + ", balanceType="
				+ balanceType + ", balanceExpDate=" + balanceExpDate
				+ ", balanceValue=" + balanceValue + "]";
	}
	
	public long getBalanceID() {
		return balanceID;
	}

	public void setBalanceID(long balanceID) {
		this.balanceID = balanceID;
	}

	public int getBalanceType() {
		return balanceType;
	}

	public void setBalanceType(int balanceType) {
		this.balanceType = balanceType;
	}

	public Timestamp getBalanceExpDate() {
		return balanceExpDate;
	}

	public void setBalanceExpDate(Timestamp balanceExpDate) {
		this.balanceExpDate = balanceExpDate;
	}

	public double getBalanceValue() {
		return balanceValue;
	}

	public void setBalanceValue(double balanceValue) {
		this.balanceValue = balanceValue;
	}
	
	
}
