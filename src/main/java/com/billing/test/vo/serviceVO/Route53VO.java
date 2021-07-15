package com.billing.test.vo.serviceVO;

import java.util.List;

import com.billing.test.vo.CalResultVO;

import lombok.Getter;
@Getter
public class Route53VO extends CalResultVO{
	
	private List<String> routingTypes;
	private List<String> routingTargets;	
	private List<String> locations;	
	
	
	public void setRoutingTypes(List<String> routingTypes) {
		this.routingTypes = routingTypes;
	}
	public void setRoutingTargets(List<String> routingTargets) {
		this.routingTargets = routingTargets;
	}
	public void setLocations(List<String> locations) {
		this.locations = locations;
	}
}

