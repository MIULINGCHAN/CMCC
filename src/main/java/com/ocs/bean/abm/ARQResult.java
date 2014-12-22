package com.ocs.bean.abm;

import java.util.ArrayList;

public class ARQResult {
	private Subscriber subsInfo;
	private ArrayList<Balance> balances;
	private ArrayList<Counter> counters;
	
	public ARQResult(Subscriber subs,ArrayList<Balance> balances, ArrayList<Counter> counters) {
		super();
		this.subsInfo = subs;
		this.balances = balances;
		this.counters = counters;
	}
	
	public ARQResult(){
		subsInfo = new Subscriber();
		balances = new ArrayList<Balance>();
		counters = new ArrayList<Counter>();
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

	@Override
	public String toString() {
		return "ARQResult [subsInfo=" + subsInfo + ", balances=" + balances
				+ ", counters=" + counters + "]";
	}

	public Subscriber getSubsInfo() {
		return subsInfo;
	}

	public void setSubsInfo(Subscriber subsInfo) {
		this.subsInfo = subsInfo;
	}
}
