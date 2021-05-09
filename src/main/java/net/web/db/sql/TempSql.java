package net.web.db.sql;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import home.db.ColumnType;
import home.db.DBConnection;
import home.db.Database;
import home.db.DbClass;
import home.db.PkCriteria;
import net.web.common.Constants;
import net.web.db.entity.TempEnt;

public class TempSql {

	private static final Logger logger = LogManager.getLogger(TempSql.class);
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public void createTable() throws IOException, SQLException, ClassNotFoundException {
		logger.debug("Creating Temperature table. Teble exist: " );
		DBConnection con = null;
		boolean exist = false;
		try {
			con = getConnection();


			exist = tableExist(con, TempEnt.TBL_NM);	
			logger.debug("Creating Temperature table. Teble exist: " + exist);

			if (!exist) {
				logger.info("Temperature table does not exist , creating");
				List<ColumnType> columns = new ArrayList<ColumnType>();					
				columns.add(new ColumnType(TempEnt.ID, true).INT().setPKCriteria(new PkCriteria().autoIncrement()));
				columns.add(new ColumnType(TempEnt.TEMP1, false).VarChar(10));
				columns.add(new ColumnType(TempEnt.TEMP1_DT).TimeStamp());
				columns.add(new ColumnType(TempEnt.TEMP2).VarChar(10));
				columns.add(new ColumnType(TempEnt.TEMP2_DT).TimeStamp());
				columns.add(new ColumnType(TempEnt.TEMP_POOL).VarChar(10));
				columns.add(new ColumnType(TempEnt.TEMP_POOL_DT).TimeStamp());
				columns.add(new ColumnType(TempEnt.TEMP_GARAGE).VarChar(10));
				columns.add(new ColumnType(TempEnt.TEMP_GARAGE_DT).TimeStamp());
				columns.add(new ColumnType(TempEnt.TMP_OBJ_UDP_DATE).TimeStamp());

				con.createTable(TempEnt.TBL_NM, columns);	

			}
		}finally {
			if (con != null) {
				con.close();
			}
		}	
	}
	public TempEnt add(TempEnt temp) throws SQLException, ClassNotFoundException{
		DBConnection con = null;

		try{
			con = getConnection();

			int key = con.buildAddQuery(TempEnt.TBL_NM)
					.setParameter(TempEnt.TEMP1, temp.getTemp1())
					.setParameter(TempEnt.TEMP1_DT, temp.getTmp1UpdDt())
					.setParameter(TempEnt.TEMP2, temp.getTemp2())
					.setParameter(TempEnt.TEMP2_DT, temp.getTmp2UpdDt())
					.setParameter(TempEnt.TEMP_POOL, temp.getTempPool())
					.setParameter(TempEnt.TEMP_POOL_DT, temp.getTmpPoolUpdDt())
					.setParameter(TempEnt.TEMP_GARAGE, temp.getTmpGarage())
					.setParameter(TempEnt.TEMP_GARAGE_DT, temp.getTmpGarageDt())
					.setParameter(TempEnt.TMP_OBJ_UDP_DATE, temp.getLastUpdated())
					.add();

			temp.setId(key);


		}finally{
			if (con!=null){
				con.close();
			}
		}
		return temp;
	}

	public TempEnt getCurrentTemp() throws ClassNotFoundException, SQLException {

		DBConnection con = null;
		TempEnt ent = null;
		try{
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + TempEnt.TBL_NM + " WHERE " + TempEnt.TMP_OBJ_UDP_DATE +"=(Select MAX("+TempEnt.TMP_OBJ_UDP_DATE+") FROM " + TempEnt.TBL_NM + "  ) " )
					.getSelectResultSet();


			while(rs.next()) {
				ent = new TempEnt(rs);
			}

		}finally{
			if (con!=null){
				con.close();
			}
		}

		return ent;
	}

	public List<TempEnt> getDateRageTemperature(LocalDateTime start, LocalDateTime end) throws ClassNotFoundException, SQLException{
		List<TempEnt> tempList  = new ArrayList<>();

		DBConnection con = null;

		try{
			con = getConnection();
			//			SELECT * FROM Products
			//			WHERE Price BETWEEN 10 AND 20;

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + TempEnt.TBL_NM + " WHERE " + TempEnt.TMP_OBJ_UDP_DATE + " BETWEEN  :startD AND :endD" )
					.setParameter("startD", start.format(formatter))
					.setParameter("endD", end.format(formatter))
					.getSelectResultSet();

			while(rs.next()) {
				TempEnt e = new TempEnt(rs);
				tempList.add(e);
			}


		}finally{
			if (con!=null){
				con.close();
			}
		}


		return tempList;
	}

	/** new
	 * Clean up the temperature db.. 
	 * 
	 * @param cleanUpFrom
	 * 			- Give a date to clean up to. So if we want to clean up the last month, the 'cleanUpFrom' would be
	 * 				 Current date (Now is 2021-04-14) - 1 month = cleanUpFrom (2021-03-14) .  So everything before that will be deleted.
	 * 			
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	//	public int cleanUpTempDbByDate(LocalDateTime cleanUpFrom) throws ClassNotFoundException, SQLException {
	//		DBConnection con = null;
	//		int deleted = 0;
	//		try {
	//			con = getConnection();
	//			String query = "DELETE FROM " + TempEnt.TBL_NAME + " where " + TempEnt.REC_DATE + " <= :date";
	//
	//			deleted = con.createSelectQuery(query)
	//			.setParameter("date", cleanUpFrom)
	//			.delete();
	//
	//		}finally {
	//			if (con != null) {
	//				con.close();
	//			}
	//
	//		}	
	//		
	//		return deleted;
	//
	//	}

	private DBConnection getConnection() throws ClassNotFoundException, SQLException{

		Database db = new Database(Constants.DB_MYSQL,Constants.DB_USER, Constants.DB_PASS, DbClass.Mysql);
		return new DBConnection(db);
	}

	private boolean tableExist(DBConnection con, String table) throws SQLException {
		DatabaseMetaData md = con.getConnection().getMetaData();
		ResultSet rs = md.getTables(null, null, table.toLowerCase(), null);

		boolean exist = rs.next();

		if (!exist) {
			//check in upper case
			rs = md.getTables(null, null, table.toUpperCase(), null);

			exist = rs.next();
		}

		return exist;
	}
}
