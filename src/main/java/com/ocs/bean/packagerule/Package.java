package com.ocs.bean.packagerule;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * 套餐(增值业务)类
 * @author Wangchao
 */
@Entity
public class Package {
	
	@PrimaryKey
	private String packageID;
	private String packageName;
	
	/** 所属品牌 */
	private String brand;
	
	private String description;
	
	//是否是增值业套餐
	private boolean isAdditionalService;
	//如果是增值业务套餐，那么其类型
	private AdditionalServiceType additionalServiceType;
	
	/** 语音业务 */
	private MobileBusiness voiceService;
	
	/** 数据流量业务 */
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
