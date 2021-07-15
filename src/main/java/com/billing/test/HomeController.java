package com.billing.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.billing.test.serviceImpl.PriceListApiParsing;
import com.billing.test.serviceImpl.CalListPrintImpl;
import com.billing.test.serviceImpl.ExplorerListApiParsing;
import com.billing.test.vo.CalResultVO;
import com.billing.test.vo.ExplorerListVO;
import com.billing.test.vo.InfoVO;
import com.billing.test.vo.serviceVO.Route53VO;
import com.billing.test.vo.AwsComDefaultVO;


@Controller
public class HomeController {	
    
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getRoutePriceList(InfoVO vo, AwsComDefaultVO priceVo, ExplorerListVO evo,Route53VO result) {	
		//init Setting	
		vo.setAccountInf();									//계정정보 설정
		vo.setStart		 ("2021-03-01");					//서비스 시작일 설정
		vo.setEnd		 ("2021-07-09");					//서비스 종료일 설정
		vo.setServiceCode("AmazonRoute53");
		vo.setServiceName("Amazon Route 53");
		//good
		//"AmazonGlacier"  	"AmazonRoute53" 	"AmazonAPIGateway" 		"AWSCertificateManager"		"AmazonCloudFront" "AmazonCloudFront"	"AmazonCloudFront"
		//"Amazon Glacier"  "Amazon Route 53" 	"Amazon API Gateway" 	"AWS Certificate Manager"   "Amazon CloudFront" "Amazon CloudFront"  "Amazon CloudFront"
		
		//not good
		// 	"AmazonCloudWatch" 문의예정	"AmazonEC2"				*필터추가예정		"AmazonS3"					"AwsDataTransfer"
		// 	"AmazonCloudWatch"			"Amazon Elastic Compute Cloud"			"Amazon Simple Storage Service"
		
		//서비스코드/네임 리스트화 예정
		
		ExplorerListApiParsing.explorerPasingJson(vo, evo);	// explorerListParsing
		PriceListApiParsing.priceParsingJson(vo, priceVo);	// priceListJsonParsing		
		CalListPrintImpl.calList(priceVo, evo, result);				// 검산식 수행
		CalListPrintImpl.calInfoPrint(result);						// 검산값 비교 출력
		CalListPrintImpl.calPrint();
		
	    return "getRoutePriceList";
	}
}