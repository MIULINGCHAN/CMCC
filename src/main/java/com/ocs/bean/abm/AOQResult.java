package com.ocs.bean.abm;

import java.util.ArrayList;

public class AOQResult {
	private ArrayList<Balance> balances;
	private ArrayList<Counter> counters;	
	private boolean deductResult;
	private boolean reserveResult;
	
	public AOQResult(){
		this.balances = new ArrayList<Balance>();
		this.counters = new ArrayList<Counter>();
		deductResult = false;
		reserveResult = false;
	}
	
	@Override
	public String toString() {
		return "AOQResult [balances=" + balances + ", counters=" + counters
				+ ", deductResult=" + deductResult + ", reserveResult="
				+ reserveResult + "]";
	}
	public ArrayList<Balance> getBalances() {
		return balances;
	}
	public void setBalances(ArrayList<Balance> balances) {
		this.balances = balances;
	}
	public ArrayList<Counter> getCounters() {
		return counters;
	}
	public void setCounters(ArrayList<Counter> counters) {
		this.counters = counters;
	}
	public boolean isDeductResult() {
		return deductResult;
	}
	public void setDeductResult(boolean deductResult) {
		this.deductResult = deductResult;
	}
	public boolean isReserveResult() {
		return reserveResult;
	}
	public void setReserveResult(boolean reserveResult) {
		this.reserveResult = reserveResult;
	}
	
	
}
