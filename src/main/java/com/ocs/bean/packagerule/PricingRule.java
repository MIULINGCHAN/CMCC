package com.ocs.bean.packagerule;

import com.sleepycat.persist.model.Persistent;

/**
 * 语音业务批价规则，是语音业务计费规则的组成部分
 * 
 * @author Wangchao
 * 
 */
@Persistent
public class PricingRule {

	/** 计费时段开始点 */
	private int periodStart;

	/** 计费时段结束点 */
	private int periodEnd;

	/** 累积时间下限 */
	private int accumulationFloor;

	/** 累积时间上限 */
	private int accumulationCeiling;

	/** 价格(语音业务单位为:元/min，数据流量业务为：元/KB */
	private double price;

	public int getPeriodStart() {
		return periodStart;
	}

	public void setPeriodStart(int periodStart) {
		this.periodStart = periodStart;
	}

	public int getPeriodEnd() {
		return periodEnd;
	}

	public void setPeriodEnd(int periodEnd) {
		this.periodEnd = periodEnd;
	}

	public int getAccumulationFloor() {
		return accumulationFloor;
	}

	public void setAccumulationFloor(int accumulationFloor) {
		this.accumulationFloor = accumulationFloor;
	}

	public int getAccumulationCeiling() {
		return accumulationCeiling;
	}

	public void setAccumulationCeiling(int accumulationCeiling) {
		this.accumulationCeiling = accumulationCeiling;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "PricingRule [periodStart=" + periodStart + ", periodEnd="
				+ periodEnd + ", accumulationFloor=" + accumulationFloor
				+ ", accumulationCeiling=" + accumulationCeiling + ", price="
				+ price + "]";
	}

}
