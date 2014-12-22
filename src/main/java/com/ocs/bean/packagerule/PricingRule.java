package com.ocs.bean.packagerule;

import com.sleepycat.persist.model.Persistent;

/**
 * ����ҵ�����۹���������ҵ��Ʒѹ������ɲ���
 * 
 * @author Wangchao
 * 
 */
@Persistent
public class PricingRule {

	/** �Ʒ�ʱ�ο�ʼ�� */
	private int periodStart;

	/** �Ʒ�ʱ�ν����� */
	private int periodEnd;

	/** �ۻ�ʱ������ */
	private int accumulationFloor;

	/** �ۻ�ʱ������ */
	private int accumulationCeiling;

	/** �۸�(����ҵ��λΪ:Ԫ/min����������ҵ��Ϊ��Ԫ/KB */
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
