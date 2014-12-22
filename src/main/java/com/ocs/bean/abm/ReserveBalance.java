package com.ocs.bean.abm;

import java.io.Serializable;
import java.sql.Timestamp;

public class ReserveBalance implements Serializable{
	private String sessionID;
	private long serviceID;
	private long serviceType;
	private long accountID;
	private long accountItemID;//balanceId
	private double reserveAmount;
	private Timestamp reserveDate;
	private Timestamp expDate;
	private Timestamp updateTime;
	
	@Override
	public String toString() {
		return "ReserveBalance [sessionID=" + sessionID + ", serviceID="
				+ serviceID + ", serviceType=" + serviceType + ", accountID="
				+ accountID + ", accountItemID=" + accountItemID
				+ ", reserveAmount=" + reserveAmount + ", reserveDate="
				+ reserveDate + ", expDate=" + expDate + ", updateTime="
				+ updateTime + "]";
	}
	
	public String getSessionID() {
		return sessionID;
	}
	public ReserveBalance() {
		super();
		this.sessionID = "";
		this.serviceID = 0;
		this.serviceType = 0;
		this.accountID = 0;
		this.accountItemID = 0;
		this.reserveAmount = 0;
		this.reserveDate = null;
		this.expDate = null;
		this.updateTime = null;
	}
	public ReserveBalance(String sessionID, long serviceID, long serviceType,
			long accountID, long accountItemID, double reserveAmount,
			Timestamp reserveDate, Timestamp expDate, Timestamp updateTime) {
		super();
		this.sessionID = sessionID;
		this.serviceID = serviceID;
		this.serviceType = serviceType;
		this.accountID = accountID;
		this.accountItemID = accountItemID;
		this.reserveAmount = reserveAmount;
		this.reserveDate = reserveDate;
		this.expDate = expDate;
		this.updateTime = updateTime;
	}
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	public long getServiceID() {
		return serviceID;
	}
	public void setServiceID(long serviceID) {
		this.serviceID = serviceID;
	}
	public long getServiceType() {
		return serviceType;
	}
	public void setServiceType(long serviceType) {
		this.serviceType = serviceType;
	}
	public long getAccountID() {
		return accountID;
	}
	public void setAccountID(long accountID) {
		this.accountID = accountID;
	}
	public long getAccountItemID() {
		return accountItemID;
	}
	public void setAccountItemID(long accountItemID) {
		this.accountItemID = accountItemID;
	}
	public double getReserveAmount() {
		return reserveAmount;
	}
	public void setReserveAmount(double reserveAmount) {
		this.reserveAmount = reserveAmount;
	}
	public Timestamp getReserveDate() {
		return reserveDate;
	}
	public void setReserveDate(Timestamp reserveDate) {
		this.reserveDate = reserveDate;
	}
	public Timestamp getExpDate() {
		return expDate;
	}
	public void setExpDate(Timestamp expDate) {
		this.expDate = expDate;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	
	
}
