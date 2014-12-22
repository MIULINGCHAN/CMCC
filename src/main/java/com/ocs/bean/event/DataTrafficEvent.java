package com.ocs.bean.event;

/**
 * 用于U/T包时，CF提取U/T包中的相关信息封装为该类对象，然后传由RF进行批价
 * 
 * @author Wang Chao
 * 
 */
public class DataTrafficEvent {
	public DataTrafficEvent(String phoneNumber, String produceLocation,
			String relativeLocation, String produceChannel,
			double produceQuantity, String produceTime_start,
			String produceTime_end) {
		super();
		this.phoneNumber = phoneNumber;
		this.produceLocation = produceLocation;
		this.relativeLocation = relativeLocation;
		this.produceChannel = produceChannel;
		this.produceQuantity = produceQuantity;
		this.produceTime_start = produceTime_start;
		this.produceTime_end = produceTime_end;
	}

	private String phoneNumber;
	private String produceLocation;
	private String relativeLocation;
	private String produceChannel;
	private double produceQuantity;
	private String produceTime_start;
	private String produceTime_end;

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getProduceLocation() {
		return produceLocation;
	}

	public void setProduceLocation(String produceLocation) {
		this.produceLocation = produceLocation;
	}

	public String getRelativeLocation() {
		return relativeLocation;
	}

	public void setRelativeLocation(String relativeLocation) {
		this.relativeLocation = relativeLocation;
	}

	public String getProduceChannel() {
		return produceChannel;
	}

	public void setProduceChannel(String produceChannel) {
		this.produceChannel = produceChannel;
	}

	public double getProduceQuantity() {
		return produceQuantity;
	}

	public void setProduceQuantity(double produceQuantity) {
		this.produceQuantity = produceQuantity;
	}

	public String getProduceTime_start() {
		return produceTime_start;
	}

	public void setProduceTime_start(String produceTime_start) {
		this.produceTime_start = produceTime_start;
	}

	public String getProduceTime_end() {
		return produceTime_end;
	}

	public void setProduceTime_end(String produceTime_end) {
		this.produceTime_end = produceTime_end;
	}

	@Override
	public String toString() {
		return "DataTrafficEvent [phoneNumber=" + phoneNumber
				+ ", produceLocation=" + produceLocation
				+ ", relativeLocation=" + relativeLocation
				+ ", produceChannel=" + produceChannel + ", produceQuantity="
				+ produceQuantity + ", produceTime_start=" + produceTime_start
				+ ", produceTime_end=" + produceTime_end + "]";
	}

}
