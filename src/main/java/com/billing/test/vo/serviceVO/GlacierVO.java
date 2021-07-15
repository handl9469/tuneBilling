package com.billing.test.vo.serviceVO;

import java.util.List;

import com.billing.test.vo.AwsComDefaultVO;

import lombok.Getter;
@Getter
public class GlacierVO extends AwsComDefaultVO{
	private List<String> location;	
	
	public void setLocation(List<String> location) {
		this.location = location;
	}
}
