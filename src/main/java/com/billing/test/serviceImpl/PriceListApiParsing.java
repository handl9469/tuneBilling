package com.billing.test.serviceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.pricing.AWSPricing;
import com.amazonaws.services.pricing.AWSPricingClientBuilder;
import com.amazonaws.services.pricing.model.Filter;
import com.amazonaws.services.pricing.model.GetProductsRequest;
import com.amazonaws.services.pricing.model.GetProductsResult;
import com.billing.test.vo.InfoVO;
import com.billing.test.vo.AwsComDefaultVO;

public class PriceListApiParsing {	
	private static final Logger logger  = LoggerFactory.getLogger(PriceListApiParsing.class);
	
	
	
	public static void priceParsingJson(InfoVO vo, AwsComDefaultVO priceVo){
		//priceList API 값을 담을 리스트 생성	
		//Common Product
		List<String> servicecodes 	= new ArrayList<String>();	//서비스코드
		List<String> servicenames 	= new ArrayList<String>();	//서비스네임
		List<String> usagetypes		= new ArrayList<String>();	//사용유형 
		//Common Terms
		List<String> units			= new ArrayList<String>();	 
		List<String> beginRanges	= new ArrayList<String>();	//사용유형 최소범위
		List<String> endRanges		= new ArrayList<String>();	//사용유형 최대범위
		List<String> currencyCodes	= new ArrayList<String>();	//화폐
		List<String> currencyRates	= new ArrayList<String>();	//단위가격		
		List<String> descriptions	= new ArrayList<String>();	//단위 설명
		
		List<String> locations		= new ArrayList<String>();	//리전정보
		AWSCredentials 	credentials = new BasicAWSCredentials(vo.getAccessKey(),vo.getSecretAccessKey());		//AWS계정 정보 담기
		AWSPricing pricing = AWSPricingClientBuilder.standard()							//엔드포인트 설정
													.withCredentials(new AWSStaticCredentialsProvider(credentials))
													.withRegion(Regions.US_EAST_1)  	//기본SDK 지역설정   
													.build();		
		GetProductsRequest getProductsRequest = new GetProductsRequest();				//요청할 priceList
		GetProductsResult  getProductsResult  = new GetProductsResult();				//받은 priceList
		
		boolean isNextTokenCheck = false;	//토큰 체크
		do{		
			//nextToken validation check
			
			if( isNextTokenCheck == false ) {
				getProductsRequest = new GetProductsRequest().withServiceCode(vo.getServiceCode());	//요청할 서비스코드 입력
				getProductsResult  = pricing.getProducts(getProductsRequest);						//priceList 등록
				logger.debug(getProductsResult.toString());
				isNextTokenCheck 	   = true;
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
	                 JSONObject terms             	= (JSONObject) jsonObject.get("terms");	                
	                 JSONObject attributes 			= (JSONObject) product.get("attributes");	                 
	                 for(Object termsKey : terms.keySet()) {							//OnDemand 와 Reserved 분리
	                	 JSONObject termsValue = (JSONObject) terms.get(termsKey);      
	                	 
	                	 for(Object onDemandkey : termsValue.keySet()) {
	                		 JSONObject onDemandValue 	= (JSONObject) termsValue.get(onDemandkey);
	                		 JSONObject priceDimensions = (JSONObject) onDemandValue.get("priceDimensions");
	                		 
	                		 for(Object priceDimensionskey : priceDimensions.keySet()) {
	                			 JSONObject priceDimensionsValue = (JSONObject) priceDimensions.get(priceDimensionskey);
	                			 JSONObject pricePerUnitMap = (JSONObject) priceDimensionsValue.get("pricePerUnit");	
	                			 servicecodes	.add((String) attributes.get("servicecode"));
	                			 servicenames	.add((String) attributes.get("servicename"));
	                			 usagetypes		.add((String) attributes.get("usagetype"));	
	                			 if(null != attributes.get("location")){
	                				 locations  .add((String) attributes.get("location"));
	                			 } else if(null == attributes.get("location") && null != attributes.get("toLocation")){
	                				 locations  .add((String) attributes.get("toLocation"));
	                			 }	else {
	                				 locations  .add("global");
	                			 }
	                			 
	                			 units			.add((String) priceDimensionsValue.get("unit"));
	                			 beginRanges	.add((String) priceDimensionsValue.get("beginRange"));	
	                			 endRanges		.add((String) priceDimensionsValue.get("endRange"));	  
	                			 currencyCodes	.add((String) pricePerUnitMap.keySet().toString());	                			
	                			 currencyRates	.add((String) pricePerUnitMap.values().toArray()[0]);	                			
	                			 descriptions	.add((String) priceDimensionsValue.get("description"));
	                			 
	                		 }
	                	 }
	                 }
	                 
	                 //VO에 저장
	                 //Common Product
	                 priceVo.setServicecodes	(servicecodes);
	                 priceVo.setServicenames	(servicenames);
	                 priceVo.setUsagetypes		(usagetypes);	
	                 //Common Terms
	                 priceVo.setUnits			(units);       
	                 priceVo.setBeginRanges		(beginRanges);
	                 priceVo.setEndRanges		(endRanges);	
	                 priceVo.setCurrencyCodes	(currencyCodes);
	                 priceVo.setCurrencyRates	(currencyRates);	                 
	                 priceVo.setDescriptions	(descriptions);	
	                 priceVo.setLocations		(locations);
                 }catch (Exception e) {
	                 logger.error("Parsing Exception {}", e.getMessage(), e);
	             }
	        }
		}while(null != getProductsResult.getNextToken()); //NextToken 없을시 종료
	}
	
//	public static void doParse(JSONObject temp) {
//		JSONObject tempValue = new JSONObject();
//		
//		for(Object tempKey : temp.keySet()) {				
//			
//			tempValue = (JSONObject) temp.get(tempKey);		
//			//attributes
//			if(null != ((HashMap) tempValue).get("attributes")) {
//				JSONObject attributes = (JSONObject) tempValue.get("attributes");
//				servicecodes.add((String) attributes.get("servicecode"));
//				servicenames.add((String) attributes.get("servicename"));
//				usagetypes	.add((String) attributes.get("usagetype"));	
//				
//			}
//			//priceDimensions
//			if(null != ((HashMap) tempValue).get("priceDimensions")) {
//				for(Object priceDimensionskey : tempValue.keySet()) {
//					JSONObject priceDimensions = (JSONObject) tempValue.get(priceDimensionskey);       			 
//					JSONObject pricePerUnitMap = (JSONObject) priceDimensions.get("pricePerUnit");	                         
//					units		 .add((String) priceDimensions.get("unit"));
//					beginRanges	 .add((String) priceDimensions.get("beginRange"));	
//					endRanges	 .add((String) priceDimensions.get("endRange"));	       			
//					descriptions .add((String) priceDimensions.get("description"));
//	       			pricePerUnits.add((String) pricePerUnitMap.values().toArray()[0]); //통화가 다르게 들어올 수 있으므로 인덱스값으로 가져옴
//	       		}
//			}
//			if(null == (JSONObject) temp.get(tempKey)) break; // 더이상 키가 없을시 종료
//			
//			if(null != ((HashMap) tempValue).get("NextToken")) break;
//				
//			
//		}
//		
//	}
}
