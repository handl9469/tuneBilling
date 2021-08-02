package com.billing.test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.amazonaws.services.costexplorer.model.ResultByTime;
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
	public String getRoutePriceList(InfoVO vo, AwsComDefaultVO priceVo, ExplorerListVO evo) {	
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
		
		List<ResultByTime> evoList = ExplorerListApiParsing.explorerPasingJson(vo, evo);	// explorerListParsing
		List<AwsComDefaultVO> pvoList = PriceListApiParsing.priceParsingJson(vo);	// priceListJsonParsing
		CalListPrintImpl.calList(pvoList, evoList);				// 검산식 수행
		CalListPrintImpl.calPrint();
		
		/////DB con
//		try { Class<?> dbDriver = Class.forName("org.mariadb.jdbc.Driver"); 
//		System.out.println("마리아디비 드라이버(" + dbDriver.toString() + ")가 로딩됨"); } 
//		catch (ClassNotFoundException e) { 
//			System.out.println("마리아디비 드라이버가 로딩되지 않음"); e.printStackTrace(); 
//			}
//		Enumeration<Driver> drivers = DriverManager.getDrivers(); 
//		if (drivers.hasMoreElements()) {
//			while (drivers.hasMoreElements()) {
//				Driver driver = drivers.nextElement(); 
//				System.out.println("드라이버 : " + driver.toString()); 
//				}
//			} else { 
//				System.out.println("드라이버가 없음"); 
//				}
//		Connection connection = null; 
//		// jdbc:mariadb://{host}[:{port}]/[{database}] 
//		String databaseConn = "jdbc:mariadb://localhost:3306/test"; 
//		try { 
//			connection = DriverManager.getConnection(databaseConn, "root", "gks9469zz@"); 
//			System.out.println("마리아디비에 연결됨"); 
//			} catch (SQLException e) {
//				System.out.println("마리아디비에 연결하지 못함"); 
//				e.printStackTrace(); 
//				} finally {
//					if (null != connection) {
//						try {
//							connection.close(); 
//							System.out.println("마리아디비에서 연결을 종료함"); 
//							} catch (SQLException e) { 
//								System.out.println("마리아디비에서 연결을 종료하지 못함"); 
//								e.printStackTrace(); 
//								} 
//						} 
//					}

		
	    return "getRoutePriceList";
	}
}