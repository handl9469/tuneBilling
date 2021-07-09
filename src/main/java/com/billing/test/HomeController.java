package com.billing.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.billing.test.serviceImpl.priceListApiParsing;
import com.billing.test.serviceImpl.calListPrintImpl;
import com.billing.test.serviceImpl.explorerListApiParsing;
import com.billing.test.vo.explorerListVO;
import com.billing.test.vo.infoVO;
import com.billing.test.vo.priceListVO;


@Controller
public class HomeController {	
    
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getRoutePriceList(infoVO vo, priceListVO priceVo, explorerListVO evo) {	
		//init Setting	
		vo.setAccountInf();									//계정정보 설정
		vo.setStart		 ("2021-03-01");					//서비스 시작일 설정
		vo.setEnd		 ("2021-07-09");					//서비스 종료일 설정
		vo.setServiceCode("AmazonEC2");  				
		vo.setServiceName("Amazon Elastic Compute Cloud");  	
		//good
		//"AmazonGlacier"  	"AmazonRoute53" 	"AmazonAPIGateway" 		"AWSCertificateManager"		"AmazonS3"
		//"Amazon Glacier"  "Amazon Route 53" 	"Amazon API Gateway" 	"AWS Certificate Manager"   "Amazon Simple Storage Service"
		
		//not good
		// 	"AmazonCloudWatch" 문의예정	"AmazonEC2"						*필터추가예정
		// 	"AmazonCloudWatch"			"Amazon Elastic Compute Cloud"	
		
		//서비스코드/네임 리스트화 예정
		
		priceListApiParsing.priceParsingJson(vo, priceVo);	// priceListJsonParsing
		explorerListApiParsing.explorerPasingJson(vo, evo);	// explorerListParsing
		calListPrintImpl.calList(priceVo, evo);				// 검산식 수행
		calListPrintImpl.calPrint();						// 검산값 비교 출력
		
		
	    return "getRoutePriceList";
	}
}