package com.coderming.controller;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.coderming.Service.DataProcessor;
import com.coderming.model.UsageLoad;

@RestController
@RequestMapping("/")
public class MonitorController {
	static Logger logger = Logger.getLogger(MonitorController.class.getName());
	
	public MonitorController() {
	}

	DataProcessor dataProcessor;
	@Autowired void setDataProcessor(DataProcessor dp) {
		dataProcessor = dp;
	}

	@RequestMapping(value = "/daily", method = RequestMethod.GET)
	Collection<UsageLoad> getDailyData(@PathVariable("server") String serverName) {
		return dataProcessor.getUsageData(serverName, true);
	}
	
	@RequestMapping(value = "/hourly", method = RequestMethod.GET)
	Collection<UsageLoad> getHourlyData(@PathVariable("server") String serverName) {
		return dataProcessor.getUsageData(serverName, false);
	}

	@RequestMapping(method = RequestMethod.GET)
	ResponseEntity<?> add(@PathVariable("server") String serverName
			, @PathVariable("cpu-usage") double cpuload
			, @PathVariable("mem-usage")double memload) {
		dataProcessor.add(serverName, cpuload, memload);
		HttpHeaders httpHeaders = new HttpHeaders();
		return new ResponseEntity<>(null, httpHeaders, HttpStatus.OK);
	}
}
