package com.ocs.bean.packagerule;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * �ײ�(��ֵҵ��)��
 * @author Wangchao
 */
@Entity
public class Package {
	
	@PrimaryKey
	private String packageID;
	private String packageName;
	
	/** ����Ʒ�� */
	private String brand;
	
	private String description;
	
	//�Ƿ�����ֵҵ�ײ�
	private boolean isAdditionalService;
	//�������ֵҵ���ײͣ���ô������
	private AdditionalServiceType additionalServiceType;
	
	/** ����ҵ�� */
	private MobileBusiness voiceService;
	
	/** ��������ҵ�� */
	private MobileBusiness dataTrafficService;

	public boolean isAdditionalService() {
		return isAdditionalService;
	}

	public void setAdditionalService(boolean isAdditionalService) {
		this.isAdditionalService = isAdditionalService;
	}

	public AdditionalServiceType getAdditionalServiceType() {
		return additionalServiceType;
	}

	public void setAdditionalServiceType(AdditionalServiceType additionalServiceType) {
		this.additionalServiceType = additionalServiceType;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageID() {
		return packageID;
	}

	public void setPackageID(String packageID) {
		this.packageID = packageID;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public MobileBusiness getVoiceService() {
		return voiceService;
	}

	public void setVoiceService(MobileBusiness voiceService) {
		this.voiceService = voiceService;
	}

	public MobileBusiness getDataTrafficService() {
		return dataTrafficService;
	}

	public void setDataTrafficService(MobileBusiness dataTrafficService) {
		this.dataTrafficService = dataTrafficService;
	}
	
	public enum AdditionalServiceType {
		DATA_TRAFFIC,
		VOICE
	}
	
	public enum ServiceType {
		DATA_TRAFFIC,
		VOICE
	}

	@Override
	public String toString() {
		return "Package [packageName=" + packageName + ", packageID="
				+ packageID + ", brand=" + brand + ", description="
				+ description + ", isAdditionalService=" + isAdditionalService
				+ ", additionalServiceType=" + additionalServiceType
				+ ", voiceService=" + voiceService + ", dataTrafficService="
				+ dataTrafficService + "]";
	}
	
}
