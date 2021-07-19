package com.billing.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.billing.test.serviceImpl.CalListPrintImpl;
import com.billing.test.serviceImpl.ExplorerListApiParsing;
import com.billing.test.serviceImpl.PriceListApiParsing;
import com.billing.test.vo.AwsComDefaultVO;
import com.billing.test.vo.CalResultVO;
import com.billing.test.vo.ExplorerListVO;
import com.billing.test.vo.InfoVO;


@Controller
public class HomeController {	
    
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getRoutePriceList(InfoVO vo, AwsComDefaultVO priceVo, ExplorerListVO evo,CalResultVO result) {	
		//init Setting	
		vo.setAccountInf();									//계정정보 설정
		vo.setStart		 ("2021-03-01");					//서비스 시작일 설정
		vo.setEnd		 ("2021-07-01");					//서비스 종료일 설정
		vo.setServiceCode("AmazonCloudWatch");
		vo.setServiceName("AmazonCloudWatch");
		//good
		//"AmazonGlacier"  	"AmazonRoute53" 	"AmazonAPIGateway" 		"AWSCertificateManager"		"AmazonCloudFront" "AmazonSES"
		//"Amazon Glacier"  "Amazon Route 53" 	"Amazon API Gateway" 	"AWS Certificate Manager"   "Amazon CloudFront" "Amazon Simple Email Service"
		
		//"AWSQueueService"						"AwsDataTransfer"
		//"Amazon Simple Queue Service"
		
		//not good
		// 	"AmazonCloudWatch" 프리티어 적용해도		"AmazonS3"							"AmazonEC2"	*필터추가예정
		// 	"AmazonCloudWatch" 검산값이 다름		"Amazon Simple Storage Service"		"Amazon Elastic Compute Cloud"	
		
		//서비스코드/네임 리스트화 예정
		
		ExplorerListApiParsing.explorerPasingJson(vo, evo);	// explorerListParsing
		PriceListApiParsing.priceParsingJson(vo, priceVo);	// priceListJsonParsing		
		CalListPrintImpl.calList(priceVo, evo, result);				// 검산식 수행
		CalListPrintImpl.calInfoPrint(result);						// 검산값 비교 출력
		CalListPrintImpl.calPrint();
		
	    return "getRoutePriceList";
	}
}