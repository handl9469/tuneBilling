package com.billing.test.serviceImpl;

import java.util.ArrayList;
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
import com.amazonaws.services.pricing.model.GetProductsRequest;
import com.amazonaws.services.pricing.model.GetProductsResult;
import com.billing.test.vo.InfoVO;
import com.billing.test.vo.AwsComDefaultVO;

public class PriceListApiParsing {	
	private static final Logger logger  = LoggerFactory.getLogger(PriceListApiParsing.class);
	
	
	
	public static List<AwsComDefaultVO> priceParsingJson(InfoVO vo){
		AwsComDefaultVO 	  priceVo   = new AwsComDefaultVO();
		List<AwsComDefaultVO> priceList = new ArrayList<AwsComDefaultVO>();
		
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
	                			 
	                			 priceVo = new AwsComDefaultVO();
	                			 priceVo.setServicecode((String) attributes.get("servicecode"));
	                			 priceVo.setServicename((String) attributes.get("servicename"));	
	                			 priceVo.setUsagetype  ((String) attributes.get("usagetype"));	
	                			 if(null != attributes.get("location")){
	                				 priceVo.setLocation((String) attributes.get("location")); 
	                			 } else if(null == attributes.get("location") && null != attributes.get("fromLocation")){
	                				 priceVo.setLocation((String) attributes.get("fromLocation")); 
	                			 }	else {
	                				 priceVo.setLocation("global");
	                			 }
	                			 
	                			 priceVo.setUnit		((String) priceDimensionsValue.get("unit"));
	                			 priceVo.setBeginRange	((String) priceDimensionsValue.get("beginRange"));	
	                			 priceVo.setEndRange	((String) priceDimensionsValue.get("endRange"));	  
	                			 priceVo.setCurrencyCode((String) pricePerUnitMap.keySet().toString());	                			
	                			 priceVo.setCurrencyRate((String) pricePerUnitMap.values().toArray()[0]);	                			
	                			 priceVo.setDescription	((String) priceDimensionsValue.get("description"));
	                			 
	                			 priceList.add(priceVo);
	                		 }
	                	 }
	                 }
                 }catch (Exception e) {
	                 logger.error("Parsing Exception {}", e.getMessage(), e);
	             }
	        }
		}while(null != getProductsResult.getNextToken()); //NextToken 없을시 종료
		return priceList;
	}
	
}
