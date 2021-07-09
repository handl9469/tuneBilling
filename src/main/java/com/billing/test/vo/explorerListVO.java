package com.billing.test.vo;

import java.util.List;

import com.amazonaws.services.costexplorer.model.ResultByTime;

import lombok.Getter;

@Getter
public class explorerListVO {
	
	List<ResultByTime> resultByTimes;
	
	public void setResultByTimes(List<ResultByTime> resultByTimes) {
		this.resultByTimes = resultByTimes;
	}
}
