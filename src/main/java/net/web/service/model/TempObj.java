package net.web.service.model;

import java.util.List;

public class TempObj {
	
	
	private String currTemp1 = null;
	private String currTemp1Date = null;
	private String currTemp2 = null;
	private String currTemp2Date = null;
	private String currPool = null;
	private String currPoolDate = null;
	
	
	private List<TempChartNumbers> tempChart;
	
	
	public String getCurrTemp1() {
		return currTemp1;
	}
	public String getCurrTemp1Date() {
		return currTemp1Date;
	}
	public String getCurrTemp2() {
		return currTemp2;
	}
	public String getCurrTemp2Date() {
		return currTemp2Date;
	}
	public String getCurrPool() {
		return currPool;
	}
	public String getCurrPoolDate() {
		return currPoolDate;
	}
	public void setCurrTemp1(String currTemp1) {
		this.currTemp1 = currTemp1;
	}
	public void setCurrTemp1Date(String currTemp1Date) {
		this.currTemp1Date = currTemp1Date;
	}
	public void setCurrTemp2(String currTemp2) {
		this.currTemp2 = currTemp2;
	}
	public void setCurrTemp2Date(String currTemp2Date) {
		this.currTemp2Date = currTemp2Date;
	}
	public void setCurrPool(String currPool) {
		this.currPool = currPool;
	}
	public void setCurrPoolDate(String currPoolDate) {
		this.currPoolDate = currPoolDate;
	}
	public List<TempChartNumbers> getTempChart() {
		return tempChart;
	}
	public void setTempChart(List<TempChartNumbers> tempChart) {
		this.tempChart = tempChart;
	}

}
