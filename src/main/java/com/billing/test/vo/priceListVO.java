package com.billing.test.vo;

import java.util.List;

import lombok.Getter;

@Getter
public class priceListVO {
	
	private List<String> usagetypes;
	private List<String> beginRanges;	
	private List<String> endRanges;
	private List<String> pricePerUnits;		
	
	
	public void setUsagetypes(List<String> usagetypes) {
		this.usagetypes = usagetypes;
	}
	public void setBeginRanges(List<String> beginRanges) {
		this.beginRanges = beginRanges;
	}
	public void setEndRanges(List<String> endRanges) {
		this.endRanges = endRanges;
	}
	public void setPricePerUnits(List<String> pricePerUnits) {
		this.pricePerUnits = pricePerUnits;
	}
}
