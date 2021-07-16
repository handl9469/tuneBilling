package com.billing.test.serviceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.costexplorer.model.DateInterval;
import com.amazonaws.services.costexplorer.model.Group;
import com.amazonaws.services.costexplorer.model.ResultByTime;
import com.billing.test.vo.AwsComDefaultVO;
import com.billing.test.vo.CalResultVO;
import com.billing.test.vo.ExplorerListVO;

public class CalListPrintImpl {
	
	static List<String> 	  monTotalPriceList 	= new ArrayList<String>(); 	  	 //월별 총가격 검산 결과 리스트
 	static List<List<String>> monUsageTypePriceList = new ArrayList<List<String>>(); //월별 사용타입별 가격 검산 결과 리스트	
 	
	public static void calList(AwsComDefaultVO pvo, ExplorerListVO evo, CalResultVO result) {
		List<String> usageTypes 			= new ArrayList<String>();
		List<String> usageQuantitys 		= new ArrayList<String>();
//		List<String> timePeriods			= new ArrayList<String>();
		List<String> startDates				= new ArrayList<String>();
		List<String> endDates				= new ArrayList<String>();
		List<String> intervalAmounts		= new ArrayList<String>();
		List<String> pricePerUnits			= new ArrayList<String>();
		List<String> usageTypePrices		= new ArrayList<String>();
		List<String> usageTypePriceTotals	= new ArrayList<String>();
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
		String startDate;
		String endDate;
		BigDecimal usageTypePriceTotal	= new BigDecimal("0");	//사용유형 검산 금액
	 	BigDecimal reduceAmount			= new BigDecimal("0");	//프리티어 절감량
		//계산방식: 사용유형별 ( 양amount(double) * 리스트값pricePerUnit(double) )를 총합한 후 반올림
	 	//위와 같은 계산식이 맞는지 판별 후 적용해야함
	 	for(ResultByTime resultByTime : evo.getResultByTimes()) {		//월별	 		
	 		List<String> tempUsagePriceList = new ArrayList<String>(); 	//유형요금별 비교값 담을 리스트 생성(월별로 사용유형 묶음)
	 		
	 		timePeriod 	= resultByTime.getTimePeriod();
	 		
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
	 					startDate	 = timePeriod.getStart();
	 			 		endDate 	 = timePeriod.getEnd();
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
	 							intervalAmount = intervalAmount.subtract(reduceAmount); // 구간사용량 = 구간사용량 - 프리티어절감량
	 						}	
 						usageTypePrice = intervalAmount.multiply(pricePerUnit);	//사용유형 구간별 가격
 						usageTypePriceTotal = usageTypePriceTotal.add(usageTypePrice).setScale(2, RoundingMode.HALF_UP);//사용유형 가격 += 단위당가격*사용량 
 						usageTypePriceTotal = usageTypePriceTotal.setScale(2, RoundingMode.HALF_UP); 				// 소수점반올림
 						isConfirm = false;
 						if(originUsageTypePrice.equals(usageTypePriceTotal)) isConfirm = true;
 						
	 					//리스트에 등록
 						usageTypes.add(usagetype);		
 						usageQuantitys.add(usageQuantity.toString());
// 						timePeriods.add(timePeriod.toString());		
 						startDates.add(startDate);
 						endDates.add(endDate);
 						intervalAmounts.add(intervalAmount.toString()); 
 						pricePerUnits.add(pricePerUnit.toString());
 						usageTypePrices.add(usageTypePrice.toString());
 						usageTypePriceTotals.add(usageTypePriceTotal.toString());
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
	 			isConfirm = false;
		 		if(originUsageTypePrice.equals(usageTypePriceTotal)) isConfirm = true;
		 		
		 		
		 		tempUsagePriceList.add("\t"+usageTypeName+"\t "+usageTypePriceTotal.toString()+"\t"+originUsageTypePrice.toString()+" \t" +isConfirm);
	 		}
	 		
	 		//월별 사용유형 검산값 담기
	 		monUsageTypePriceList.add(tempUsagePriceList);
	 		//월별 총합 검산 확인
	 		isConfirm = false;
	 		if(originTotalPrice.equals(calTotalPrice)) isConfirm = true;	
	 		monTotalPriceList.add(resultByTime.getTimePeriod() +"    "+calTotalPrice.toString()+"   \t"+originTotalPrice.toString()+" \t" +isConfirm);
	 	}
	 	result.setUsageTypes(usageTypes);
	 	result.setUsageQuantitys(usageQuantitys);
//	 	result.setTimePeriods(timePeriods);
	 	result.setStartDates(startDates);	 	
	 	result.setEndDates(endDates);
	 	result.setIntervalAmount(intervalAmounts);
	 	result.setPricePerUnits(pricePerUnits);
	 	result.setUsageTypePrices(usageTypePrices);
	 	result.setUsageTypePriceTotals(usageTypePriceTotals);
	 	result.setOriginUsageTypePrices(originUsageTypePrices);
	 	result.setIsConfirms(isConfirms);
	 	result.setBeginRanges(beginRanges);
	 	result.setEndRanges(endRanges);
	 	result.setCurrencyCodes(currencyCodes);
	 	result.setUnits(units);
	 	result.setDescriptions(descriptions);
	 	result.setLocations(locations);
	}
	
	public static void calInfoPrint(CalResultVO vo) {
		int cnt = 0;
	 		System.out.println("*************************************************************************************");	
	 		System.out.println("[1]시작일 /[2]종료일 /[3]유형타입 /[4]사용량 /[5]구간사용량 /[6]단위가격 /[7]구간가격 /[8]검산누적가격 /[9]청구서가격 /[10]검산확인 /[11]최소범위 /[12]최대범위 /[13]통화 /[14]유형단위 [15]리전정보 [16]설명 ");
	 		System.out.println();
	 		for(String str : vo.getUsagetypes()) {
	 			System.out.println(vo.getStartDates().get(cnt)+"\t"+vo.getEndDates().get(cnt)+"\t"+str+"\t"+vo.getUsageQuantitys().get(cnt)+"\t"+vo.getIntervalAmount().get(cnt)+"\t"+ vo.getPricePerUnits().get(cnt)+"\t"+ vo.getUsageTypePrices().get(cnt)+"\t"+vo.getUsageTypePriceTotals().get(cnt)+"\t"+vo.getOriginUsageTypePrices().get(cnt)+"\t"+vo.getIsConfirms().get(cnt)+"\t"+vo.getBeginRanges().get(cnt)+"\t"+vo.getEndRanges().get(cnt)+"\t"+vo.getCurrencyCodes().get(cnt)+"\t"+vo.getUnits().get(cnt)+"\t"+vo.getLocations().get(cnt)+"\t"+vo.getDescriptions().get(cnt));
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
