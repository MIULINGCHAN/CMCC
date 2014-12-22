package com.ocs.bean.abm;

public class DeductCounter {
	private long counterID;
	private double chgValue;
	
	public DeductCounter() {
		super();
	}
	public DeductCounter(long counterID, double chgValue) {
		super();
		this.counterID = counterID;
		this.chgValue = chgValue;
	}
	
	@Override
	public String toString() {
		return "DeductCounter [counterID=" + counterID + ", chgValue="
				+ chgValue + "]";
	}
	public long getCounterID() {
		return counterID;
	}
	public void setCounterID(long counterID) {
		this.counterID = counterID;
	}
	public double getChgValue() {
		return chgValue;
	}
	public void setChgValue(double chgValue) {
		this.chgValue = chgValue;
	}
	
//	public void printObject(){
//		System.out.println("-- DeductCounter Object: --");
//		System.out.println("id£º" + counterID);
//		System.out.println("changeValue£º" + chgValue);
//		System.out.println("-- *************** --");
//	}
	
	
}	
