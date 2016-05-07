package com.coderming.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class UsageLoad {
	@Id 
	@GeneratedValue
	private Long id;
	
	private Date start;
	private Date end;
	private Double cpu;
	private Double mem;
	
	UsageLoad() {	
	}

	public UsageLoad(Date start, Date end, double cpu, double mem) {
		this.start = start;
		this.end = end;
		this.cpu = cpu;
		this.mem= mem;;
	}
	public Long getId() {
		return id;
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	public Double getCpu() {
		return cpu;
	}

	public Double getMem() {
		return mem;
	}
}
