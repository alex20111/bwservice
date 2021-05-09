package net.web.db.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TempEnt {
	
	public static String TBL_NM = "temperature";
	public static String ID = "id";
	public static String TEMP1 		= "temp1";
	public static String TEMP1_DT 	= "temp1Dt";
	public static String TEMP2 		= "temp2";
	public static String TEMP2_DT 	= "temp2Dt";
	public static String TEMP_POOL 	= "tempPool";
	public static String TEMP_POOL_DT = "tempPoolDt";
	public static String TEMP_GARAGE = "tempGarage";
	public static String TEMP_GARAGE_DT = "tempGarageDt";
	public static String TMP_OBJ_UDP_DATE = "tempObjUpd";
	
	private  int id = -1;
	private  String temp1 = "-99";
	private  LocalDateTime tmp1UpdDt; 
	private  String temp2 = "-99";
	private  LocalDateTime tmp2UpdDt;
	private  String tempPool = "-99";
	private  LocalDateTime tmpPoolUpdDt;
	private  String tmpGarage  = "-99";
	private  LocalDateTime tmpGarageDt;
	
	private LocalDateTime lastUpdated;
	
	public TempEnt() {}
	
	public TempEnt(ResultSet rs) throws SQLException {
		this.id = rs.getInt(ID);
		this.temp1 = rs.getString(TEMP1);
		Timestamp tm1Upd = rs.getTimestamp(TEMP1_DT);
		this.tmp1UpdDt = tm1Upd != null ? tm1Upd.toLocalDateTime() : null;
		this.temp2 = rs.getString(TEMP2);
		Timestamp tm2Upd = rs.getTimestamp(TEMP2_DT);
		this.tmp2UpdDt = tm2Upd != null ? tm2Upd.toLocalDateTime() : null;
		this.tempPool = rs.getString(TEMP_POOL);
		Timestamp tmPoolUpd = rs.getTimestamp(TEMP_POOL_DT);
		this.tmpPoolUpdDt = tmPoolUpd != null ? tmPoolUpd.toLocalDateTime() : null;
		this.tmpGarage = rs.getString(TEMP_GARAGE);
		Timestamp tmGarageUpd = rs.getTimestamp(TEMP_GARAGE_DT);
		this.tmpGarageDt = tmGarageUpd != null ? tmGarageUpd.toLocalDateTime() : null;
		
		Timestamp lstUpd = rs.getTimestamp(TMP_OBJ_UDP_DATE);
		this.lastUpdated = lstUpd != null ? lstUpd.toLocalDateTime() : null;
	}

	public String getTemp1() {
		return temp1;
	}

	public String getTemp2() {
		return temp2;
	}

	public void setTemp1(String temp1) {
		this.temp1 = temp1;
	}

	public void setTemp2(String temp2) {
		this.temp2 = temp2;
	}

	public String getTempPool() {
		return tempPool;
	}
	public String getTmpGarage() {
		return tmpGarage;
	}

	public void setTempPool(String tempPool) {
		this.tempPool = tempPool;
	}
	public void setTmpGarage(String tmpGarage) {
		this.tmpGarage = tmpGarage;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDateTime getTmp1UpdDt() {
		return tmp1UpdDt;
	}

	public LocalDateTime getTmp2UpdDt() {
		return tmp2UpdDt;
	}

	public LocalDateTime getTmpPoolUpdDt() {
		return tmpPoolUpdDt;
	}

	public LocalDateTime getTmpGarageDt() {
		return tmpGarageDt;
	}

	public void setTmp1UpdDt(LocalDateTime tmp1UpdDt) {
		this.tmp1UpdDt = tmp1UpdDt;
	}

	public void setTmp2UpdDt(LocalDateTime tmp2UpdDt) {
		this.tmp2UpdDt = tmp2UpdDt;
	}

	public void setTmpPoolUpdDt(LocalDateTime tmpPoolUpdDt) {
		this.tmpPoolUpdDt = tmpPoolUpdDt;
	}

	public void setTmpGarageDt(LocalDateTime tmpGarageDt) {
		this.tmpGarageDt = tmpGarageDt;
	}

	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Override
	public String toString() {
		return "TempEnt [id=" + id + ", temp1=" + temp1 + ", tmp1UpdDt=" + tmp1UpdDt + ", temp2=" + temp2
				+ ", tmp2UpdDt=" + tmp2UpdDt + ", tempPool=" + tempPool + ", tmpPoolUpdDt=" + tmpPoolUpdDt
				+ ", tmpGarage=" + tmpGarage + ", tmpGarageDt=" + tmpGarageDt + ", lastUpdated=" + lastUpdated + "]";
	}
}
