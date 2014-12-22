package com.ocs.bean.account;

public class RuleUsage {
	public String ruleID;
	public String ruleName;
	public double remainQuantity; // unit : k
	
	public RuleUsage(){
		ruleID = "";
		ruleName = "";
		remainQuantity = 0;
	}
	
	public RuleUsage(String ruleID, String ruleName, double remainQuantity) {
		super();
		this.ruleID = ruleID;
		this.ruleName = ruleName;
		this.remainQuantity = remainQuantity;
	}
	public String getRuleID() {
		return ruleID;
	}


	public void setRuleID(String ruleID) {
		this.ruleID = ruleID;
	}


	public String getRuleName() {
		return ruleName;
	}


	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}


	public double getRemainQuantity() {
		return remainQuantity;
	}


	public void setRemainQuantity(double remainQuantity) {
		this.remainQuantity = remainQuantity;
	}


	@Override
	public String toString() {
		return "RuleUsage [ruleID=" + ruleID + ", ruleName=" + ruleName
				+ ", remainQuantity=" + remainQuantity + "]";
	}
	
	
}
