package com.billing.test.vo;

import lombok.Data;

@Data
public class CalResultVO{
	
private String usagetype;
	
	private String usageQuantity;
	
	private String startDate;
	
	private String endDate;
	
	private String intervalAmount;
	
	private String pricePerUnit;
	
	private String usageTypePrice;
	
	private String originUsageTypePrice;
	
	private String isConfirm;
	
	private String usageTypePriceTotal;
	//Common Product
	private String servicecode;
	private String servicename;

	//Common Terms
	private String unit;
	private String beginRange;
	private String endRange;
	private String currencyCode;
	private String currencyRate;
	private String description;
	
	private String location;
}
