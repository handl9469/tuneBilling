package com.billing.test.serviceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.costexplorer.model.DateInterval;
import com.amazonaws.services.costexplorer.model.Group;
import com.amazonaws.services.costexplorer.model.ResultByTime;
import com.billing.test.vo.CalResultVO;
import com.billing.test.vo.ExplorerListVO;
import com.billing.test.vo.serviceVO.Route53VO;
import com.billing.test.vo.AwsComDefaultVO;

public class CalListPrintImpl {
	
	static List<String> 	  monTotalPriceList 	= new ArrayList<String>(); 	  	 //월별 총가격 검산 결과 리스트
 	static List<List<String>> monUsageTypePriceList = new ArrayList<List<String>>(); //월별 사용타입별 가격 검산 결과 리스트	
 	
	public static void calList(AwsComDefaultVO pvo, ExplorerListVO evo, Route53VO result) {
		List<String> usageTypes 			= new ArrayList<String>();
		List<String> usageQuantitys 		= new ArrayList<String>();
		List<String> timePeriods			= new ArrayList<String>();
		List<String> intervalAmounts		= new ArrayList<String>();
		List<String> pricePerUnits			= new ArrayList<String>();
		List<String> usageTypePrices		= new ArrayList<String>();
		List<String> originUsageTypePrices	= new ArrayList<String>();
		List<String> isConfirms				= new ArrayList<String>();
		
		List<String> beginRanges			= new ArrayList<String>();
		List<String> endRanges				= new ArrayList<String>();
		List<String> currencyCodes			= new ArrayList<String>();
		List<String> units					= new ArrayList<String>();
		List<String> descriptions			= new ArrayList<String>();
		List<String> locations				= new ArrayList<String>();
		
		
		BigDecimal calTotalPrice		= new BigDecimal("0"); 	//검산 총 금액
		BigDecimal originTotalPrice		= new BigDecimal("0");	//원래 총 금액		
		BigDecimal usageQuantity		= new BigDecimal("0");	//사용자 사용량
		DateInterval timePeriod;								//검색간격
		BigDecimal intervalAmount 		= new BigDecimal("0");	//구간사용량
		BigDecimal pricePerUnit			= new BigDecimal("0");	//단위 가격
		BigDecimal usageTypePrice		= new BigDecimal("0");	//사용유형 구간별 검산 금액
		BigDecimal originUsageTypePrice = new BigDecimal("0");	//원래 사용유형 금액	
		boolean isConfirm = false;								//검산확인
		BigDecimal beginRange			= new BigDecimal("0");	//사용최소범위
		BigDecimal endRange				= new BigDecimal("0");	//사용최대범위
		String currencyCode;									//통화
		String unit;											//유형단위					
		String description;										//설명
		String location;										//리전정보
		
		BigDecimal usageTypePriceTotal	= new BigDecimal("0");	//사용유형 검산 금액
	 	BigDecimal reduceAmount			= new BigDecimal("0");	//프리티어 절감량
		//계산방식: 사용유형별 ( 양amount(double) * 리스트값pricePerUnit(double) )를 총합한 후 반올림
	 	//위와 같은 계산식이 맞는지 판별 후 적용해야함
	 	for(ResultByTime resultByTime : evo.getResultByTimes()) {		//월별	 		
	 		List<String> tempUsagePriceList = new ArrayList<String>(); 	//유형요금별 비교값 담을 리스트 생성(월별로 사용유형 묶음)
	 		
	 		timePeriod = resultByTime.getTimePeriod();
	 		calTotalPrice	= new BigDecimal("0");				//검산할 총합값 초기화
	 		originTotalPrice= new BigDecimal("0");				//Explorer 총합값 초기화
	 		intervalAmount	= new BigDecimal("0");				//구간사용량
	 		for(Group group : resultByTime.getGroups()) {		//월별 사용유형값
	 			String usageTypeName = group.getKeys().get(0);	//검산할 사용유형 이름
	 			usageTypePriceTotal 		 = new BigDecimal("0");		//검산할 사용유형 가격 초기화	 			
	 			originUsageTypePrice = new BigDecimal(group.getMetrics().get("UnblendedCost").getAmount()); //Exploerer 사용유형별 요금
	 			originUsageTypePrice = originUsageTypePrice.setScale(2, RoundingMode.HALF_UP); 				// 소수점반올림
	 			
	 			//Exploerer 총요금
	 			originTotalPrice = originTotalPrice.add(originUsageTypePrice).setScale(2, RoundingMode.HALF_UP);//Explore 유형요금 가져오기	 		
	 			
		 		int pListIdxCnt = 0; //priceList 인덱스 카운트
	 			for(String usagetype : pvo.getUsagetypes()) {
	 				//priceList 사용유형과 비교
	 				if(usagetype.equals(group.getKeys().get(0)) ) {
	 					currencyCode = pvo.getCurrencyCodes().get(pListIdxCnt);
	 					unit 		 = pvo.getUnits().get(pListIdxCnt);
	 					description  = pvo.getDescriptions().get(pListIdxCnt);
	 					location	 = pvo.getLocations().get(pListIdxCnt);
	 					
	 					pricePerUnit = new BigDecimal(pvo.getCurrencyRates().get(pListIdxCnt));				//priceList Unit당 가격 담기	 					
	 					beginRange   = new BigDecimal(pvo.getBeginRanges().get(pListIdxCnt));				//priceList 최소범위	 					
	 					usageQuantity= new BigDecimal(group.getMetrics().get("UsageQuantity").getAmount()); //사용자 사용량
	 					
	 					//각 사용량 구간별 단위가격으로 나누어 합산한 가격을 usageTypePrice에 담는다.
	 					//사용최소범위 <= 사용자 사용량
	 					if(0 <= usageQuantity.compareTo(beginRange)){					
	 						if(!pvo.getEndRanges().get(pListIdxCnt).equals("Inf")) { // 최대값 범위가 INF가 아닐때, 즉 최대값이 정해져 있을때
	 							endRange   	= new BigDecimal(pvo.getEndRanges().get(pListIdxCnt));//priceList 최대범위
	 								//사용최소범위 <= 사용자 사용량 < 사용최대범위
		 							if(-1 == usageQuantity.compareTo(endRange)) {
		 								intervalAmount = usageQuantity.subtract(beginRange);//사용량 = 사용자 사용량 - 사용최소범위
		 								
		 							}else {	//사용최대범위 <= 사용자 사용량
		 								intervalAmount = endRange.subtract(beginRange);		//사용량 = 사용최대범위 - 사용최소범위		 								
		 							}
		 							reduceAmount = FreeTierCalInfo.FreeTierApply(pvo,usagetype,beginRange,endRange,intervalAmount); //프리티어 적용
				 					
	 						}else { //최대값 범위가 INF일때
	 								intervalAmount = usageQuantity.subtract(beginRange);
	 								reduceAmount = FreeTierCalInfo.FreeTierApply(pvo,usagetype,intervalAmount); //프리티어 적용
	 						}
	 						//구간량 < 프리티어절감량 => 0으로 치환
	 						if(reduceAmount.compareTo(intervalAmount) >= 0) {
	 							intervalAmount = BigDecimal.ZERO;
	 						}else {
	 							intervalAmount = intervalAmount.subtract(reduceAmount);
	 						}	
 						usageTypePrice = intervalAmount.multiply(pricePerUnit);	//사용유형 구간별 가격
 						usageTypePriceTotal = usageTypePriceTotal.add(usageTypePrice).setScale(2, RoundingMode.HALF_UP);//사용유형 가격 += 단위당가격*사용량 
 						usageTypePriceTotal = usageTypePriceTotal.setScale(2, RoundingMode.HALF_UP); 				// 소수점반올림
 						if(originUsageTypePrice.equals(usageTypePriceTotal)) isConfirm = true;
 						
	 					//리스트에 등록
 						usageTypes.add(usagetype);		
 						usageQuantitys.add(usageQuantity.toString());
 						timePeriods.add(timePeriod.toString());		
 						intervalAmounts.add(intervalAmount.toString()); 
 						pricePerUnits.add(pricePerUnit.toString());
 						usageTypePrices.add(usageTypePrice.toString());
 						originUsageTypePrices.add(originUsageTypePrice.toString());	
 						isConfirms.add(isConfirm+"");
 						beginRanges.add(beginRange.toString());
 						endRanges.add(endRange.toString());
 						currencyCodes.add(currencyCode);
 						units.add(unit);
 						descriptions.add(description);
 						locations.add(location);
	 					}
	 					
	 				}
	 				pListIdxCnt++;
	 			}
	 			
	 			calTotalPrice = calTotalPrice.add(usageTypePriceTotal);// 검사총합에 사용유형별 값 추가
	 			
	 			//사용유형별 검산 확인
		 		
		 		if(originUsageTypePrice.equals(usageTypePriceTotal)) isConfirm = true;
		 		
		 		
		 		tempUsagePriceList.add("\t"+usageTypeName+"\t "+usageTypePriceTotal.toString()+"\t"+originUsageTypePrice.toString()+" \t" +isConfirm);
	 		}
	 		
	 		//월별 사용유형 검산값 담기
	 		monUsageTypePriceList.add(tempUsagePriceList);
	 		//월별 총합 검산 확인
	 		if(originTotalPrice.equals(calTotalPrice)) isConfirm = true;	
	 		monTotalPriceList.add(resultByTime.getTimePeriod() +"    "+calTotalPrice.toString()+"   \t"+originTotalPrice.toString()+" \t" +isConfirm);
	 	}
	 	result.setUsageTypes(usageTypes);
	 	result.setUsageQuantitys(usageQuantitys);
	 	result.setTimePeriods(timePeriods);
	 	result.setIntervalAmount(intervalAmounts);
	 	result.setPricePerUnits(pricePerUnits);
	 	result.setUsageTypePrices(usageTypePrices);
	 	result.setOriginUsageTypePrices(originUsageTypePrices);
	 	result.setIsConfirms(isConfirms);
	 	result.setBeginRanges(beginRanges);
	 	result.setEndRanges(endRanges);
	 	result.setCurrencyCodes(currencyCodes);
	 	result.setUnits(units);
	 	result.setDescriptions(descriptions);
	 	result.setLocations(locations);
	}
	
	public static void calInfoPrint(Route53VO vo) {
		int cnt = 0;
	 		System.out.println("*************************************************************************************");	
	 		System.out.println("[1]유형타입 /[2]사용량 /[3]검색간격 /[4]구간사용량 /[5]단위가격 /[6]검산가격 /[7]원래가격 /[8]검산확인 /[9]최소범위 /[10]최대범위 /[11]통화 /[12]유형단위 /[13]리전정보 /[14]설명 ");
	 		System.out.println();
	 		for(String str : vo.getUsagetypes()) {
	 			System.out.println("[1]"+str +"\t[2]"+ vo.getUsageQuantitys().get(cnt)+"\t[3]"+vo.getTimePeriods().get(cnt)+"\t[4]"+ vo.getIntervalAmount().get(cnt)+"\t[5]"+ vo.getPricePerUnits().get(cnt)+"\t[6]"+ vo.getUsageTypePrices().get(cnt)+"\t[7]"+vo.getOriginUsageTypePrices().get(cnt)+"\t[8]"+vo.getIsConfirms().get(cnt)+"\t[9]"+vo.getBeginRanges().get(cnt)+"\t[10]"+vo.getEndRanges().get(cnt)+"\t[11]"+vo.getCurrencyCodes().get(cnt)+"\t[12]"+vo.getUnits().get(cnt)+"\t[13]"+vo.getLocations().get(cnt)+"\t[14]"+vo.getDescriptions().get(cnt));
	 			cnt++;
	 		}
	 		
	 		

	 		cnt++;
	 		System.out.println();
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
