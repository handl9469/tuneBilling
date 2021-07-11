package com.billing.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.billing.test.serviceImpl.PriceListApiParsing;
import com.billing.test.serviceImpl.CalListPrintImpl;
import com.billing.test.serviceImpl.ExplorerListApiParsing;
import com.billing.test.vo.ExplorerListVO;
import com.billing.test.vo.InfoVO;
import com.billing.test.vo.PriceListVO;


@Controller
public class HomeController {	
    
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getRoutePriceList(InfoVO vo, PriceListVO priceVo, ExplorerListVO evo) {	
		//init Setting	
		vo.setAccountInf();									//계정정보 설정
		vo.setStart		 ("2021-03-01");					//서비스 시작일 설정
		vo.setEnd		 ("2021-07-09");					//서비스 종료일 설정
		vo.setServiceCode("AmazonEC2");  				
		vo.setServiceName("Amazon Elastic Compute Cloud");  	
		//good
		//"AmazonGlacier"  	"AmazonRoute53" 	"AmazonAPIGateway" 		"AWSCertificateManager"		"AmazonCloudFront" "AmazonCloudFront"	"AmazonCloudFront"
		//"Amazon Glacier"  "Amazon Route 53" 	"Amazon API Gateway" 	"AWS Certificate Manager"   "Amazon CloudFront" "Amazon CloudFront"  "Amazon CloudFront"
		
		//not good
		// 	"AmazonCloudWatch" 문의예정	"AmazonEC2"				*필터추가예정		"AmazonS3"	
		// 	"AmazonCloudWatch"			"Amazon Elastic Compute Cloud"			"Amazon Simple Storage Service"
		
		//서비스코드/네임 리스트화 예정
		
		PriceListApiParsing.priceParsingJson(vo, priceVo);	// priceListJsonParsing
		ExplorerListApiParsing.explorerPasingJson(vo, evo);	// explorerListParsing
		CalListPrintImpl.calList(priceVo, evo);				// 검산식 수행
		CalListPrintImpl.calPrint();						// 검산값 비교 출력
		
		
	    return "getRoutePriceList";
	}
}