package com.billing.test.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.pricing.AWSPricing;
import com.amazonaws.services.pricing.AWSPricingClientBuilder;
import com.amazonaws.services.pricing.model.GetProductsRequest;
import com.amazonaws.services.pricing.model.GetProductsResult;
import com.billing.test.vo.infoVO;
import com.billing.test.vo.priceListVO;

public class priceListApiParsing {	
	private static final Logger logger  = LoggerFactory.getLogger(priceListApiParsing.class);
	
	public static void priceParsingJson(infoVO vo, priceListVO priceVo){
		//priceList API 값을 담을 리스트 생성
		List<String> usagetypes		= new ArrayList<String>(); 							//사용유형
		List<String> pricePerUnits	= new ArrayList<String>();							//단위가격
		List<String> beginRanges	= new ArrayList<String>();							//사용유형 최소범위
		List<String> endRanges		= new ArrayList<String>();							//사용유형 최대범위
		
		
		AWSPricing pricing = AWSPricingClientBuilder.standard()							//엔드포인트 설정
													.withRegion(Regions.US_EAST_1)  	//기본SDK 지역설정   
													.build();		
		GetProductsRequest getProductsRequest = new GetProductsRequest();				//요청할 priceList
		GetProductsResult  getProductsResult  = new GetProductsResult();				//받은 priceList
		
		boolean nextTokenCheck = false;	//토큰 체크
		do{		
			//nextToken validation check
			if( nextTokenCheck == false ) {
				//첫 실행시	
				getProductsRequest = new GetProductsRequest().withServiceCode(vo.getServiceCode());	//요청할 서비스코드 입력
				getProductsResult  = pricing.getProducts(getProductsRequest);						//priceList 등록
				nextTokenCheck 	   = true;
			}else {	
				//이후 실행시 NextToken 값을 이용 다음 페이지 priceList 정보 가져옴
				getProductsRequest = new GetProductsRequest().withServiceCode(vo.getServiceCode())				//요청할 서비스코드 입력
															 .withNextToken(getProductsResult.getNextToken());  //가져올 페이지 토큰 입력
				getProductsResult  = pricing.getProducts(getProductsRequest);		 							//priceList 등록
			}			
			
			//가져온 priceList JSON-simple을 이용하여 Parsing
	        for(String price : getProductsResult.getPriceList()) {
	        	 try {
	        		 JSONParser parser 				= new JSONParser();
	                 Object obj         			= parser.parse(price);				//JSON Parsing 리턴객체를 object로 받음
	                 																	//필요한 값들은 JSONObject타입임
	                 JSONObject jsonObject        	= (JSONObject) obj;
	                 JSONObject product           	= (JSONObject) jsonObject.get("product");
	                 JSONObject attributes 			= (JSONObject) product.get("attributes");
	                 JSONObject terms             	= (JSONObject) jsonObject.get("terms");
	                 for(Object termsKey : terms.keySet()) {
	                	 JSONObject tempValue = (JSONObject) terms.get(termsKey);                 
	                	 for(Object onDemandkey : tempValue.keySet()) {
	                		 JSONObject onDemandValue 	= (JSONObject) tempValue.get(onDemandkey);
	                		 JSONObject priceDimensions = (JSONObject) onDemandValue.get("priceDimensions");
	                		 
	                		 for(Object priceDimensionskey : priceDimensions.keySet()) {
	                			 JSONObject priceDimensionsValue = (JSONObject) priceDimensions.get(priceDimensionskey);
	                			 usagetypes		.add((String) attributes.get("usagetype"));	
	                			 beginRanges	.add((String) priceDimensionsValue.get("beginRange"));	
	                			 endRanges		.add((String) priceDimensionsValue.get("endRange"));
	                			 JSONObject pricePerUnitMap = (JSONObject) priceDimensionsValue.get("pricePerUnit");	                         
	                			 pricePerUnits	.add((String) pricePerUnitMap.values().toArray()[0]); //통화가 다르게 들어올 수 있으므로 인덱스값으로 가져옴
	                		 }
	                	 }
	                 }
	                 
	                 //VO에 저장
	                 priceVo.setUsagetypes	 (usagetypes);
	                 priceVo.setBeginRanges	 (beginRanges);
	                 priceVo.setEndRanges	 (endRanges);
	                 priceVo.setPricePerUnits(pricePerUnits);
	                 
	                 
                 }catch (Exception e) {
	                 logger.error("Parsing Exception {}", e.getMessage(), e);
	             }
	        }
		}while(null != getProductsResult.getNextToken()); //NextToken 없을시 종료
	}
}
