package com.ocs.bean.abm;

import java.util.ArrayList;

public class AOQParams {
	private String sessionID;
	private String subscriberID;
	private String accountID;
	private ArrayList<DeductBalance> deductBalances;
	private ArrayList<DeductCounter> deductCounters;
	private ArrayList<ReserveBalance> reserveBalances;
	
	public AOQParams(){
		this.sessionID = "";
		this.subscriberID = "";
		this.accountID = "";
		this.deductBalances = new ArrayList<DeductBalance>();
		this.deductCounters = new ArrayList<DeductCounter>();
		this.reserveBalances = new ArrayList<ReserveBalance>();
	}
	
	public AOQParams(String sessionID, String subscriberID, String accountID,
			ArrayList<ReserveBalance> reserveBalances) {
		super();
		this.sessionID = sessionID;
		this.subscriberID = subscriberID;
		this.accountID = accountID;
		this.reserveBalances = reserveBalances;
	}

	public AOQParams(String sessionID, String subscriberID, String accountID,
			ArrayList<DeductBalance> deductBalances,
			ArrayList<DeductCounter> deductCounters,
			ArrayList<ReserveBalance> reserveBalances) {
		super();
		this.sessionID = sessionID;
		this.subscriberID = subscriberID;
		this.accountID = accountID;
		this.deductBalances = deductBalances;
		this.deductCounters = deductCounters;
		this.reserveBalances = reserveBalances;
	}

	public AOQParams(String sessionID, String subscriberID, String accountID,
			ArrayList<DeductBalance> deductBalances,
			ArrayList<DeductCounter> deductCounters) {
		super();
		this.sessionID = sessionID;
		this.subscriberID = subscriberID;
		this.accountID = accountID;
		this.deductBalances = deductBalances;
		this.deductCounters = deductCounters;
	}

	public String getSessionID() {
		return sessionID;
	}
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	public String getSubscriberID() {
		return subscriberID;
	}
	public void setSubscriberID(String subscriberID) {
		this.subscriberID = subscriberID;
	}
	public String getAccountID() {
		return accountID;
	}
	public void setAccountID(String accountID) {
		this.accountID = accountID;
	}
	public ArrayList<DeductBalance> getDeductBalances() {
		return deductBalances;
	}
	public void setDeductBalances(ArrayList<DeductBalance> deductBalances) {
		this.deductBalances = deductBalances;
	}
	public ArrayList<DeductCounter> getDeductCounters() {
		return deductCounters;
	}
	public void setDeductCounters(ArrayList<DeductCounter> deductCounters) {
		this.deductCounters = deductCounters;
	}
	public ArrayList<ReserveBalance> getReserveBalances() {
		return reserveBalances;
	}
	public void setReserveBalances(ArrayList<ReserveBalance> reserveBalances) {
		this.reserveBalances = reserveBalances;
	}
	@Override
	public String toString() {
		return "AOQParams [sessionID=" + sessionID + ", subscriberID="
				+ subscriberID + ", accountID=" + accountID
				+ ", deductBalances=" + deductBalances + ", deductCounters="
				+ deductCounters + ", reserveBalances=" + reserveBalances + "]";
	}
	
	
}
