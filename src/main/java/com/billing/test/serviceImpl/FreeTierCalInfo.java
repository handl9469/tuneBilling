package com.billing.test.serviceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.billing.test.vo.AwsComDefaultVO;

public class FreeTierCalInfo {
//	<Trackable AWS Free Tier services>
//	*Amazon Simple Storage Service
//	Global-Requests-Tier1		
//	Global-Requests-Tier2
//	Global-TimedStorage-ByteHrs
//	
//	*Amazon API Gateway
//	Global-ApiGatewayRequest
//	
//	*Amazon CloudFront	
//	Global-DataTransfer-Out-Bytes
//	Global-Requests-Tier1
//		
//	*Amazon CloudWatch	
//	Global-CW:Requests
//	Global-DataProcessing-Bytes
//	Global-TimedStorage-ByteHrs
//		
//	*Amazon Elastic Compute Cloud	
//	Global-BoxUsage:freetier.micro
//	Global-BoxUsage:freetier.micro
//	Global-DataProcessing-Bytes
//	Global-EBS:SnapshotUsage
//	Global-EBS:VolumeIOUsage
//	Global-EBS:VolumeUsage
//	Global-LCUUsage
//	Global-LoadBalancerUsage
//		
//	*Amazon Simple Email Service	
//	Global-Message
//	Global-Recipients-EC2
//		
//	*Amazon Simple Queue Service
//	Global-Requests
//		
//	*AWSDataTransfer	
//	Global-DataTransfer-Out-Bytes
	
	//프리티어로 인해 절감될 interval Amount 계산	(endRange != Inf)
	public static BigDecimal FreeTierApply(AwsComDefaultVO pvo, String usagetype, BigDecimal beginRange, BigDecimal endRange, BigDecimal intervalAmount) {
		List<String> freeTierList = getFreeTierList(pvo);
//		BigDecimal freeBeginRange			= new BigDecimal("0");	//프리티어 사용최소범위는 0으로 전제함
//		BigDecimal freeEndRange				= new BigDecimal("0");	//프리티어 사용최대범위
		BigDecimal reduceAmount				= new BigDecimal("0");  //프리티어로 인한 절감량
		
		String cmp1; //프리티어 usageType
		String cmp2= "";
		if(usagetype.indexOf("-") >= 0) {
			cmp2 = usagetype.substring(usagetype.indexOf("-")); //비교할 usageType -> 리전정보 제거
		}else {
			cmp2 = ("-").concat(usagetype);
		}
				
		for(String str : freeTierList) {
			cmp1 = str.substring(str.indexOf("-"));
			//프리티어 검색
			if(cmp2.equals(cmp1)) {
				//프리티어 정보 가져오기
				int pListIdxCnt = 0;
				for(String temp : pvo.getUsagetypes()) {
					if(str.equals(temp)) {
						//프리티어 최대범위가 Inf일 경우				
						if(("Inf").equals(pvo.getEndRanges().get(pListIdxCnt))){
							reduceAmount = intervalAmount;
							return reduceAmount;
						}
						BigDecimal	freeEndRange = new BigDecimal(pvo.getEndRanges().get(pListIdxCnt));	
							//프리티어 최대범위 < 사용최대범위
							if(-1 == freeEndRange.compareTo(endRange)){
								reduceAmount = freeEndRange.subtract(beginRange);
								return reduceAmount;
							}
							//사용최대범위 <= 프리티어 최대범위
							else {								
								reduceAmount = endRange.subtract(beginRange);
								return reduceAmount;
							}
					}
					pListIdxCnt++;
				}
			}
		}
		return reduceAmount;
	}
	//프리티어로 인해 절감될 interval Amount 계산	(endRange == Inf)
	public static BigDecimal FreeTierApply(AwsComDefaultVO pvo, String usagetype, BigDecimal intervalAmount) {
		List<String> freeTierList = getFreeTierList(pvo);
		BigDecimal reduceAmount				= new BigDecimal("0");  //프리티어로 인한 절감량
		
		String cmp1; //프리티어 usageType
		String cmp2= "";
		if(usagetype.indexOf("-") >= 0) {
			cmp2 = usagetype.substring(usagetype.indexOf("-")); //비교할 usageType -> 리전정보 제거
		}else {
			cmp2 = ("-").concat(usagetype);
		}
		
		int pListIdxCnt=0;				
		for(String str : freeTierList) {
			cmp1 = str.substring(str.indexOf("-"));
			if(cmp2.equals(cmp1)) {
				//프리티어 정보 가져오기
				pListIdxCnt = 0;
				for(String temp : pvo.getUsagetypes()) {
					if(str.equals(temp)) {
						//프리티어가 Inf일 경우
						if(("Inf").equals(pvo.getEndRanges().get(pListIdxCnt))){
							reduceAmount = intervalAmount;
							return reduceAmount;
						}else {
							reduceAmount = new BigDecimal(pvo.getEndRanges().get(pListIdxCnt));	
							return reduceAmount;
						}
					}
					pListIdxCnt++;
				}	
			}
			
		}
		return reduceAmount;
	}
	
	
	public static List<String> getFreeTierList(AwsComDefaultVO pvo) {
		List<String> freeTierList = new ArrayList<String>();
		for(String str : pvo.getUsagetypes()) {
			if(str.startsWith("Global-")) freeTierList.add(str);
		}
		
		return freeTierList;
	}
}
