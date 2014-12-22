package com.ocs.bean.abm;

public class DeductBalance {
	private long accountID;
	private long balanceID;
	private long balanceType;
	private long accountItemType;
	private double chgValue;
	private int clearReserveIndicator;
	
	public DeductBalance() {
		super();
	}
	public DeductBalance(long accountID, long balanceID, long balanceType,
			long accountItemType, double chgValue, int clearReserveIndicator) {
		super();
		this.accountID = accountID;
		this.balanceID = balanceID;
		this.balanceType = balanceType;
		this.accountItemType = accountItemType;
		this.chgValue = chgValue;
		this.clearReserveIndicator = clearReserveIndicator;
	}
	@Override
	public String toString() {
		return "DeductBalance [accountID=" + accountID + ", balanceID="
				+ balanceID + ", balanceType=" + balanceType
				+ ", accountItemType=" + accountItemType + ", chgValue="
				+ chgValue + ", clearReserveIndicator=" + clearReserveIndicator
				+ "]";
	}
	public long getAccountID() {
		return accountID;
	}
	public void setAccountID(long accountID) {
		this.accountID = accountID;
	}
	public long getBalanceID() {
		return balanceID;
	}
	public void setBalanceID(long balanceID) {
		this.balanceID = balanceID;
	}
	public long getBalanceType() {
		return balanceType;
	}
	public void setBalanceType(long balanceType) {
		this.balanceType = balanceType;
	}
	public long getAccountItemType() {
		return accountItemType;
	}
	public void setAccountItemType(long accountItemType) {
		this.accountItemType = accountItemType;
	}
	public double getChgValue() {
		return chgValue;
	}
	public void setChgValue(double chgValue) {
		this.chgValue = chgValue;
	}
	public int getClearReserveIndicator() {
		return clearReserveIndicator;
	}
	public void setClearReserveIndicator(int clearReserveIndicator) {
		this.clearReserveIndicator = clearReserveIndicator;
	}
	

}
