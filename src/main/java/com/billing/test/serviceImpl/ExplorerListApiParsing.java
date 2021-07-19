package com.billing.test.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.costexplorer.AWSCostExplorer;
import com.amazonaws.services.costexplorer.AWSCostExplorerClientBuilder;
import com.amazonaws.services.costexplorer.model.DateInterval;
import com.amazonaws.services.costexplorer.model.DimensionValues;
import com.amazonaws.services.costexplorer.model.DimensionValuesWithAttributes;
import com.amazonaws.services.costexplorer.model.Expression;
import com.amazonaws.services.costexplorer.model.GetCostAndUsageRequest;
import com.amazonaws.services.costexplorer.model.GetCostAndUsageResult;
import com.amazonaws.services.costexplorer.model.GetDimensionValuesRequest;
import com.amazonaws.services.costexplorer.model.GetDimensionValuesResult;
import com.amazonaws.services.costexplorer.model.Group;
import com.amazonaws.services.costexplorer.model.GroupDefinition;
import com.amazonaws.services.costexplorer.model.ResultByTime;
import com.billing.test.vo.ExplorerListVO;
import com.billing.test.vo.InfoVO;

public class ExplorerListApiParsing {

	public static void explorerPasingJson(InfoVO vo, ExplorerListVO evo) {
		AWSCredentials 	credentials = new BasicAWSCredentials(vo.getAccessKey(),vo.getSecretAccessKey());		//AWS계정 정보 담기
		AWSCostExplorer costExplorer = AWSCostExplorerClientBuilder.standard()
				 												   .withCredentials(new AWSStaticCredentialsProvider(credentials))
				 												   .withRegion(Regions.US_EAST_1)				//기본SDK 지역설정 
				 												   .build();
		
		DateInterval 	dateInterval 	= new DateInterval().withStart(vo.getStart()).withEnd(vo.getEnd()); //검색기간 설정
		DimensionValues dimensionValues = null;
		if("AwsDataTransfer".equals(vo.getServiceName()) || "Amazon Elastic Compute Cloud".equals(vo.getServiceName()) ){
			dimensionValues = new DimensionValues().withKey	("USAGE_TYPE")							//키값 설정
					   							  .withValues(getcostExplorerUsageType(vo));	
		}else {
			dimensionValues = new DimensionValues().withKey	("SERVICE")							//키값 설정
					.withValues(vo.getServiceName());			//서비스네임
		}
		
		Expression filter = new Expression().withDimensions(dimensionValues);
		  
		List<GroupDefinition> groupDefinitions = new ArrayList<GroupDefinition>();
		GroupDefinition 		groupDefinition  = new GroupDefinition().withType("DIMENSION")					//GroupBy 필터설정
																		.withKey("USAGE_TYPE");					//사용유형별
		groupDefinitions.add(groupDefinition);
		
		List<String> metrics = new ArrayList<String>();
	    metrics.add("UnblendedCost");	//사용자 지불 비용
	    metrics.add("UsageQuantity");	//사용자 이용량
	    
	    GetCostAndUsageRequest getCostAndUsageRequest = new GetCostAndUsageRequest()							//Explorer 정보 요청
	              .withTimePeriod	(dateInterval)		//기간 설정
	              .withGranularity	("MONTHLY")			//월별
	              .withFilter		(filter)			//필터 설정: 서비스
	              .withGroupBy		(groupDefinitions)	//GroupBy 설정: 사용유형별
	              .withMetrics		(metrics);			//표출내용: 사용자 지불 비용, 사용자 이용량
	    GetCostAndUsageResult getCostAndUsageResult = costExplorer.getCostAndUsage(getCostAndUsageRequest);		//결과 리턴 객체 담기
	    List<ResultByTime> resultByTimes = getCostAndUsageResult.getResultsByTime();
	    
	    //VO에 저장
	    
	    System.out.println(resultByTimes.toString());
	    
	    for(ResultByTime resultByTime : resultByTimes) {
	    	 System.out.println(resultByTime.getTimePeriod());
	    	 for(Group group : resultByTime.getGroups()) {
	    		 System.out.println(group);
	    	 }
	    	 System.out.println();
	    }
	   
	    
	    evo.setResultByTimes(resultByTimes);	
	}
	public static List<String> getcostExplorerUsageType(InfoVO vo) {
	      List<String> usageTypes    = new ArrayList<String>(); // 일정 기간 동안 지정된 필터에 대해 사용 가능한 모든 필터 값을 넣기 위한 String형 리스트를 생성한다.
	      
	      // Amazon Web Services에 요청하기 위해 AWS 자격 증명 제공을 제공하는 AWSCredentials 객체를 생성한다.
	      AWSCredentials 	credentials = new BasicAWSCredentials(vo.getAccessKey(),vo.getSecretAccessKey());		//AWS계정 정보 담기
		  AWSCostExplorer costExplorer = AWSCostExplorerClientBuilder.standard()
					 												 .withCredentials(new AWSStaticCredentialsProvider(credentials))
					 												 .withRegion(Regions.US_EAST_1)					//기본SDK 지역설정 
					 												 .build();

		  DateInterval dateInterval = new DateInterval().withStart(vo.getStart()).withEnd(vo.getEnd()); //검색기간 설정
	      
	      GetDimensionValuesRequest getDimensionValuesRequest = new GetDimensionValuesRequest() // 일정 기간 동안 지정된 필터에 대해 사용 가능한 모든 필터 값을 검색하기 위한 GetDimensionValuesRequest 객체를 생성한다.
													            .withContext("COST_AND_USAGE")  // 호출 컨텍스트 기본 값은 COST_AND_USAGE, GetCostAndUsage 작업에 사용 가능
													            .withDimension("USAGE_TYPE")
													            .withTimePeriod(dateInterval);  // 기간 설정
	      
	      GetDimensionValuesResult getDimensionValuesResult = costExplorer.getDimensionValues(getDimensionValuesRequest); // 일정 기간 동안 지정된 필터에 대해 사용 가능한 모든 필터 값을 담을 GetDimensionValuesResult 객체를 생성한다.
	      
	      for(DimensionValuesWithAttributes dimensionValuesWithAttributes : getDimensionValuesResult.getDimensionValues()) {   // 요청을 필터링하는 데 사용한 필터의 목록들을 순회합니다.
	         String value = dimensionValuesWithAttributes.getValue();             		 // 요청을 필터링하는 데 사용한 필터의 값을 가져온다.
	         
	         if(vo.getServiceName().equals("AwsDataTransfer")) {
	        	 if(value.contains("AWS-Out-Bytes") || value.contains("DataTransfer")) { // 필터의 값이 Out 또는 DataTransfer를 포함하는지 확인한다.
	 	            usageTypes.add(value);                                     			 // usageTypes의 리스트에 필터의 값을 넣어준다.
	 	         }
	         } else if(vo.getServiceName().equals("Amazon Elastic Compute Cloud")) {
	        	 if(value.contains("EBS")) { 											 // 필터의 값이 Out 또는 DataTransfer를 포함하는지 확인한다.
		 	            usageTypes.add(value);                                     		 // usageTypes의 리스트에 필터의 값을 넣어준다.
		 	         }
	         }
	      }
	      
	      usageTypes.remove("AP-DataTransfer-Out-Bytes");                   // usageTypes리스트에서 필터의 값이  AP-DataTransfer-Out-Bytes인 객체를 삭제한다.
	      
	      return usageTypes;                                        		// usageTypes리스트를 반환한다.
	   }
}
