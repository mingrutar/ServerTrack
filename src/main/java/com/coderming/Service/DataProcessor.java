package com.coderming.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.coderming.controller.MonitorController;
import com.coderming.model.UsageLoad;

@Service
public class DataProcessor {
	static Logger logger = Logger.getLogger(MonitorController.class.getName());
	
	final static long MINUTE = 60 * 1000;	// in milli
	final static long HOUR = 60 * MINUTE;	// in millisec;

	class HourlyUsageData {
		final int HourMinute = 60;
		
		Date timestamp;
		double[] cpu;				// unit is minute
		double[] mem;
		volatile int currentIdx;
		
		HourlyUsageData() {
			cpu = new double[HourMinute];
			mem = new double[HourMinute];
			reset();
		}
		void reset() {
			currentIdx = 0;
			Arrays.fill(cpu, 0);
			Arrays.fill(mem, 0);
		}
		double[] calcHourlyAverage(int toIdx, HourlyUsageData prev) {
			logger.debug(String.format("+++HourlyUsageData::calcHourlyAverage: currentIdx=%d, toIdx=%d",currentIdx, toIdx));
			int i = 0;
			double[] ret = new double[2]; 
			for (; i < toIdx; i++) {
				ret[0] += cpu[i];
				ret[1] += mem[i];
			}
			for (; i < HourMinute; i++) {
				ret[0] += prev.cpu[i];
				ret[1] += prev.mem[i];
			}
			ret[0] /= HourMinute;
			ret[1] /= HourMinute;
			return ret;
		}
		List<UsageLoad> getData( HourlyUsageData prev) {
			logger.debug(String.format("---HourlyUsageData::getData: currentIdx=%d",currentIdx));
			List<UsageLoad> ret = new ArrayList<>();
			Date endMinute = timestamp;
			Date startMinute = new Date();
			int i;
			int last = currentIdx-1;
			if (last < 0)
				last = HourMinute - 1;
			for (i = last; i >= 0; i--) {
				startMinute.setTime(endMinute.getTime() - MINUTE);
				ret.add( new UsageLoad(startMinute, endMinute, cpu[i], mem[i] ) );
				endMinute = startMinute;
			}
			for (i = HourMinute-1; i >= currentIdx; i--) {
				startMinute.setTime(endMinute.getTime() - MINUTE);
				ret.add( new UsageLoad(startMinute, endMinute, prev.cpu[i], prev.mem[i] ) );
				endMinute = startMinute;
			}
			return ret;
		}
		boolean update(Deque<double[]> rawData) {
			double minCpu = 0, minMem = 0;
			int count = 0;
			timestamp = new Date();
			double[] input = null;
			while (!rawData.isEmpty()) {
				input = rawData.pollFirst();
				minCpu += input[0];
				minMem += input[1];
				++count;
			}
			cpu[currentIdx] = minCpu/count;
			mem[currentIdx] = minMem/count;
			boolean bret = (++currentIdx == HourMinute);
			if (bret)
				currentIdx = 0;
			return bret;
		}
	}
	class DailyUsageData {
		HourlyUsageData[] hourData;
		int currentIdx;
		
		DailyUsageData() {
			hourData = new HourlyUsageData[25];
			for (int i = 0; i < 25; i++) {
				hourData[i] = new HourlyUsageData();
			}
		}
		List<UsageLoad> getByDay() {
			List<UsageLoad> ret = new ArrayList<>();
			Date endHour = hourData[currentIdx].timestamp;
			Date startHour = new Date();
			int toIdx = hourData[currentIdx].currentIdx;
			for (int i = 1; i < 24; i++) {
				int idx = (currentIdx - i );
				if (idx < 0)
					idx += 25;
				int prev = (idx >0) ? idx-1 : 24;
				logger.debug(String.format("+++DailyUsageData::getByDay call hourData[idx].calcHourlyAverage: idx=%d,prev=%d",idx, prev));
				double[] usage = hourData[idx].calcHourlyAverage(toIdx, hourData[prev]);
				startHour.setTime(endHour.getTime() - HOUR);
				ret.add( new UsageLoad(startHour, endHour, usage[0], usage[1]));
				endHour = startHour;
			}
			return ret;
		}
		List<UsageLoad> getByHour() {
			int prev = (currentIdx > 0) ? (currentIdx -1) : 24;
			logger.debug(String.format("---DailyUsageData::getByHour call hourData[idx].getData: currentIdx=%d,prev=%d",currentIdx, prev));
			return hourData[currentIdx].getData(hourData[prev]);
		}
		void update(Deque<double[]> rawData) {
			if (hourData[currentIdx].update(rawData)) {		// 
				if (currentIdx + 1 == hourData.length) {
					currentIdx = 0;
				} else {
					currentIdx++;
				}
				hourData[currentIdx].reset();
			}
		}
	}
	
	private ConcurrentMap<String, Deque<double[]>> rawData;
	private Map<String, DailyUsageData>  dailyLoad;

	public DataProcessor() {
		rawData = new ConcurrentHashMap<>();
		dailyLoad = new HashMap<>(); 
	}
	
	public Collection<UsageLoad> getUsageData(String server, boolean isDaily) {		//false = hourly
		if (dailyLoad.containsKey(server)) {
			if (isDaily) {
				return dailyLoad.get(server).getByDay();
			} else {
				return dailyLoad.get(server).getByHour();
			}
		}
		return new ArrayList<UsageLoad>();
	}
	public void add(String server, double cpu, double mem) {
		Deque<double[]> list = null;
		if (!rawData.containsKey(server)) {
			list = new LinkedList<>(); 	
			rawData.putIfAbsent(server,  list);
		} else {
			list = rawData.get(server);
		}
		list.offerLast(new double[] {cpu, mem});
	}
	@Scheduled(fixedRate = 60000)							// in ms,		
	public void processRawData() {
		logger.debug(" --processRawData is called");
		for (Map.Entry<String, Deque<double[]>> entry : rawData.entrySet() ) {
			String server = entry.getKey();
			if (!entry.getValue().isEmpty()) {
				DailyUsageData usage;
				if (!dailyLoad.containsKey(server)) {
					usage = new DailyUsageData();
					dailyLoad.put(server, usage);
				} else 
					usage = dailyLoad.get(server);
				usage.update(entry.getValue());
			}
		}
	}
}