package net.web.service.model;

public class TempChartNumbers {
	
	private String temp1 = null;
	private String temp1Date = null;
	private String temp2 = null;
	private String temp2Date = null;
	private String pool = null;
	private String poolDate = null;
	
	private String recordedDateTime = "";
	
	public TempChartNumbers() {}
	public TempChartNumbers(String temp1, String temp1Dt, String temp2, String temp2Dt, String pool, String poolDt) {
		this.temp1 = temp1;
		this.temp1Date = temp1Dt;
		this.temp2 = temp2;
		this.temp2Date = temp2Dt;
		this.pool = pool;
		this.poolDate = poolDt;
	}

	public String getTemp1() {
		return temp1;
	}

	public String getTemp1Date() {
		return temp1Date;
	}

	public String getTemp2() {
		return temp2;
	}

	public String getTemp2Date() {
		return temp2Date;
	}

	public String getPool() {
		return pool;
	}

	public String getPoolDate() {
		return poolDate;
	}

	public void setTemp1(String temp1) {
		this.temp1 = temp1;
	}

	public void setTemp1Date(String temp1Date) {
		this.temp1Date = temp1Date;
	}

	public void setTemp2(String temp2) {
		this.temp2 = temp2;
	}

	public void setTemp2Date(String temp2Date) {
		this.temp2Date = temp2Date;
	}

	public void setPool(String pool) {
		this.pool = pool;
	}

	public void setPoolDate(String poolDate) {
		this.poolDate = poolDate;
	}
	public String getRecordedDateTime() {
		return recordedDateTime;
	}
	public void setRecordedDateTime(String recordedDateTime) {
		this.recordedDateTime = recordedDateTime;
	}
	@Override
	public String toString() {
		return "TempChartNumbers [temp1=" + temp1 + ", temp1Date=" + temp1Date + ", temp2=" + temp2 + ", temp2Date="
				+ temp2Date + ", pool=" + pool + ", poolDate=" + poolDate + "]";
	}

}
