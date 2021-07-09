package com.billing.test.serviceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.costexplorer.model.Group;
import com.amazonaws.services.costexplorer.model.ResultByTime;
import com.billing.test.vo.explorerListVO;
import com.billing.test.vo.priceListVO;

public class calListPrintImpl {
	
	static List<String> 	  monTotalPriceList 	= new ArrayList<String>(); 	  	 //월별 총가격 검산 결과 리스트
 	static List<List<String>> monUsageTypePriceList = new ArrayList<List<String>>(); //월별 사용타입별 가격 검산 결과 리스트	
 	
	public static void calList(priceListVO pvo, explorerListVO evo) {
				
	 	//계산방식: 사용유형별 ( 양amount(double) * 리스트값pricePerUnit(double) )를 소수점 2번째자리까지 반올림 한후 총합 = Total
	 	//위와 같은 계산식이 맞는지 판별 후 적용해야함
	 	for(ResultByTime resultByTime : evo.getResultByTimes()) {		//월별	 		
	 		List<String> tempUsagePriceList = new ArrayList<String>(); 	//유형요금별 비교값 담을 리스트 생성(월별로 사용유형 묶음)
	 		BigDecimal calTotalPrice	= new BigDecimal("0");			//검산할 총합값 초기화
	 		BigDecimal originTotalPrice = new BigDecimal("0");			//Explorer 총합값 초기화
	 		BigDecimal amount 			= new BigDecimal("0");	
	 		for(Group group : resultByTime.getGroups()) {		 		//월별 사용유형값
	 			String usageTypeName = group.getKeys().get(0);			//검산할 사용유형 이름
	 			BigDecimal usageTypePrice = new BigDecimal("0");		//검산할 사용유형 가격 초기화	 			
	 			BigDecimal originUsageTypePrice = new BigDecimal(group.getMetrics().get("UnblendedCost").getAmount()); //Exploerer 사용유형별 요금
	 			originUsageTypePrice = originUsageTypePrice.setScale(2, RoundingMode.HALF_UP); // 소수점반올림
	 			
	 			//Exploerer 총요금
	 			originTotalPrice = originTotalPrice.add(originUsageTypePrice).setScale(2, RoundingMode.HALF_UP);//Explore 유형요금 가져오기	 		
	 			
		 		int pListIdxCnt = 0; //priceList 인덱스 카운트
	 			for(String usagetype : pvo.getUsagetypes()) {
	 				//priceList 사용유형과 비교
	 				if(usagetype.equals(group.getKeys().get(0)) ) {
	 					BigDecimal pricePerUnit	= new BigDecimal(pvo.getPricePerUnits().get(pListIdxCnt));				//priceList Unit당 가격 담기	 					
	 					BigDecimal beginRange   = new BigDecimal(pvo.getBeginRanges().get(pListIdxCnt));				//priceList 최소범위	 					
	 					BigDecimal UsageQuantity= new BigDecimal(group.getMetrics().get("UsageQuantity").getAmount());  //사용자 사용량
	 					
	 					//각 사용량 구간별 단위가격으로 나누어 합산한 가격을 usageTypePrice에 담는다.
	 					//사용최소범위 <= 사용자 사용량
	 					if(0 <= UsageQuantity.compareTo(beginRange)){	 						
	 						if(!pvo.getEndRanges().get(pListIdxCnt).equals("Inf")) { // 최대값 범위가 INF가 아닐때, 즉 최대값이 정해져 있을때
	 							BigDecimal endRange   	= new BigDecimal(pvo.getEndRanges().get(pListIdxCnt));				//priceList 최대범위
	 								//사용최소범위 <= 사용자 사용량 < 사용최대범위
		 							if(-1 == UsageQuantity.compareTo(endRange)) {
				 						amount = UsageQuantity.subtract(beginRange); //사용량 = 사용자 사용량 - 사용최소범위	
		 							}else {	//사용최대범위 <= 사용자 사용량		 								
		 								amount = endRange.subtract(beginRange);//사용량 = 사용최대범위 - 사용최소범위
		 							}
	 						}else { //최대값 범위가 INF일때
		 						amount = UsageQuantity.subtract(beginRange);
	 							}
	 						BigDecimal temp = amount.multiply(pricePerUnit);
	 						usageTypePrice = usageTypePrice.add(temp).setScale(2, RoundingMode.HALF_UP);   //add(amount.multiply(pricePerUnit));//사용유형 가격 += 단위당가격*사용량 
	 						}
	 					}
	 				pListIdxCnt++;
	 				}
	 			
	 			calTotalPrice = calTotalPrice.add(usageTypePrice);// 검사총합에 사용유형별 값 추가
	 			//사용유형별 검산 확인
		 		boolean check = false;
		 		if(originUsageTypePrice.equals(usageTypePrice)) check = true;	
		 		tempUsagePriceList.add("\t"+usageTypeName+"\t "+usageTypePrice.toString()+"\t"+originUsageTypePrice.toString()+" \t" +check);
	 		}
	 		//월별 사용유형 검산값 담기
	 		monUsageTypePriceList.add(tempUsagePriceList);
	 		//월별 총합 검산 확인
	 		boolean check = false;	
	 		if(originTotalPrice.equals(calTotalPrice)) check = true;	
	 		monTotalPriceList.add(resultByTime.getTimePeriod() +"    "+calTotalPrice.toString()+"   \t"+originTotalPrice.toString()+" \t" +check);
	 	}
	}
	
	public static void calPrint() {
		int cnt = 0;
	 	for(String str : monTotalPriceList) {
	 		System.out.println("*****************************************************************");	
	 		System.out.println("서비스 이용일 \t\t\t       검산요금   원래요금 \t확인      ");
	 		System.out.println(str);
	 		System.out.println("*****************************************************************");
	 		System.out.println("\t사용유형 \t\t검산요금  원래요금    확인      ");		 		
	 		for(String strr : monUsageTypePriceList.get(cnt)) {		 			
		    	System.out.println(strr);
		    }
	 		cnt++;
	 		System.out.println();
	 	}
	}
}
