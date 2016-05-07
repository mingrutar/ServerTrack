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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

	@RequestMapping(value = "/daily/{server}", method = RequestMethod.GET)
	public @ResponseBody Collection<UsageLoad> getDailyData(@PathVariable("server") String serverName) {
		return dataProcessor.getUsageData(serverName, true);
	}
	
	@RequestMapping(value = "/hourly/{server}", method = RequestMethod.GET)
	public @ResponseBody Collection<UsageLoad> getHourlyData(@PathVariable("server") String serverName) {
		return dataProcessor.getUsageData(serverName, false);
	}

	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody String hello(@RequestParam("test") String test) {
		return "test=" + test;
	}
	
	@RequestMapping(value="/{server}", method = RequestMethod.GET)
	public ResponseEntity<?> add(@PathVariable("server") String serverName,
			@RequestParam("cpu") String cpu, @RequestParam("mem") String mem) {
		double cpuload = Double.parseDouble(cpu);
		double memload = Double.parseDouble(mem);
		dataProcessor.add(serverName, cpuload, memload);
		HttpHeaders httpHeaders = new HttpHeaders();
		return new ResponseEntity<>(null, httpHeaders, HttpStatus.OK);
	}
}
