package com.ocs.bean.abm;

public class Subscriber {
	private int subscriberIdType;
	private String subscriberIdData;
	private String belongArea;
	private String acctID;
	
	public Subscriber(){
		setSubscriberIdType(0);
		setSubscriberIdData("");
		setBelongArea("");
		setAcctID("");
	}
	
	public Subscriber(int subscriberIdType, String subscriberIdData,
			String belongArea, String acctID) {
		super();
		this.subscriberIdType = subscriberIdType;
		this.subscriberIdData = subscriberIdData;
		this.belongArea = belongArea;
		this.acctID = acctID;
	}

	public String getAcctID() {
		return acctID;
	}

	public void setAcctID(String acctID) {
		this.acctID = acctID;
	}

	public int getSubscriberIdType() {
		return subscriberIdType;
	}

	public void setSubscriberIdType(int subscriberIdType) {
		this.subscriberIdType = subscriberIdType;
	}

	public String getSubscriberIdData() {
		return subscriberIdData;
	}

	public void setSubscriberIdData(String subscriberIdData) {
		this.subscriberIdData = subscriberIdData;
	}

	public String getBelongArea() {
		return belongArea;
	}

	public void setBelongArea(String belongArea) {
		this.belongArea = belongArea;
	}
	
	@Override
	public String toString() {
		return "Subscriber [subscriberIdType=" + subscriberIdType
				+ ", subscriberIdData=" + subscriberIdData + ", belongArea="
				+ belongArea + ", acctID=" + acctID + "]";
	}

}
