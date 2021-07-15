package com.billing.test.vo;

import java.util.List;

import lombok.Getter;

@Getter
public class CalResultVO {
	List<String> usagetypes;
	List<String> usageQuantitys;
	List<String> timePeriods;
	List<String> intervalAmount;
	List<String> pricePerUnits;
	List<String> usageTypePrices;
	List<String> originUsageTypePrices;
	List<String> isConfirms;
	List<String> beginRanges;
	List<String> endRanges;	
	List<String> currencyCodes;
	List<String> units;	
	List<String> descriptions;
	
	List<String> locations;
	
	public void setUsageTypes(List<String> usagetypes) {
		this.usagetypes = usagetypes;
	}
	public void setUsageQuantitys(List<String> usageQuantitys) {
		this.usageQuantitys = usageQuantitys;
	}
	public void setTimePeriods(List<String> timePeriods) {
		this.timePeriods = timePeriods;
	}
	public void setIntervalAmount(List<String> intervalAmount) {
		this.intervalAmount = intervalAmount;
	}
	public void setPricePerUnits(List<String> pricePerUnits) {
		this.pricePerUnits = pricePerUnits;
	}
	public void setUsageTypePrices(List<String> usageTypePrices) {
		this.usageTypePrices = usageTypePrices;
	}
	public void setOriginUsageTypePrices(List<String> originUsageTypePrices) {
		this.originUsageTypePrices = originUsageTypePrices;
	}
	public void setIsConfirms(List<String> isConfirms) {
		this.isConfirms = isConfirms;
	}
	public void setDescriptions(List<String> descriptions) {
		this.descriptions = descriptions;
	}
	public void setCurrencyCodes(List<String> currencyCodes) {
		this.currencyCodes = currencyCodes;
	}
	public void setUnits(List<String> units) {
		this.units = units;
	}
	public void setBeginRanges(List<String> beginRanges) {
		this.beginRanges = beginRanges;
	}
	public void setEndRanges(List<String> endRanges) {
		this.endRanges = endRanges;
	}
	public void setLocations(List<String> locations) {
		this.locations = locations;
	}
}
