package net.web.db.sql;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import home.db.ColumnType;
import home.db.DBConnection;
import home.db.Database;
import home.db.DbClass;
import home.db.PkCriteria;
import net.web.common.Constants;
import net.web.common.PasswordManager;
import net.web.db.entity.User;
import net.web.db.entity.UserWeb;
import net.web.enums.AccessLevel;
import net.web.enums.Website;

public class UserSql {

	private static final Logger logger = LogManager.getLogger(UserSql.class);


	public boolean CreateUserTable() throws ClassNotFoundException, SQLException, IOException {

		logger.info("CreateUserTable");

		DBConnection con = null;
		boolean exist = false;
		try {
			con = getConnection();
			
			exist = tableExist(con, User.TBL_NAME);	

			logger.debug("User table exist: " +  exist);
			if (!exist) {
				logger.info("User table does not exist , creating");
				List<ColumnType> columns = new ArrayList<ColumnType>();					
				columns.add(new ColumnType(User.ID, true).INT().setPKCriteria(new PkCriteria().autoIncrement()));
				columns.add(new ColumnType(User.USER_NAME, false).VarChar(200));
				columns.add(new ColumnType(User.PASSWORD, false).VarChar(500));
				columns.add(new ColumnType(User.FIRST_NAME, false).VarChar(50));
				columns.add(new ColumnType(User.LAST_NAME, false).VarChar(50));
				columns.add(new ColumnType(User.EMAIL, false).VarChar(300));
				columns.add(new ColumnType(User.LAST_LOGIN, false).TimeStamp());
				columns.add(new ColumnType(User.NBR_TRIES, false).INT());
				columns.add(new ColumnType(User.AUTH_TOKEN, false).VarChar(1000)); 

				con.createTable(User.TBL_NAME, columns);	

			}
			boolean userWebExist = tableExist(con, UserWeb.TBL_NAME);	
			
			if (!userWebExist) {
				logger.info("User web does not exist , creating");
				List<ColumnType> columns = new ArrayList<ColumnType>();					
				columns.add(new ColumnType(UserWeb.ID, true).INT().setPKCriteria(new PkCriteria().autoIncrement()));
				columns.add(new ColumnType(UserWeb.ACCESS_LEVEL, false).VarChar(40));
				columns.add(new ColumnType(UserWeb.WEBSITES, false).VarChar(40));
				columns.add(new ColumnType(UserWeb.USER_ID, false).INT());

				con.createTable(UserWeb.TBL_NAME, columns);	

			}
			
			
			
			if (!exist && !userWebExist) {
				logger.info("Creating new user. User: admin, Password: 1323323");
				
				User user = new User();
				user.setEmail("Changeme@yahoo.com");
				user.setUserName("admin");
				user.setFirstName("The");
				user.setLastName("ADMIN");
				String hashedPassword = PasswordManager.hashPassword("f91d42802ee289a22f96e09e41192bd958d8d7104762988460dfca506fde45db9e36898a422bf6d1de02e5494b3bac4bdeff2cece0525dcfbadd1ca2c112e235");
				user.setPassword(hashedPassword);
				user.setLastLogin(new Date());
				
				UserWeb uw  = new UserWeb();
				uw.setAccessLevel(AccessLevel.ADMIN);
				uw.setWebsiteAccess(Website.SERVICE_WEBSITE);
				List<UserWeb> uwList = new ArrayList<UserWeb>();
				uwList.add(uw);
				user.setUserWeb(uwList);
				
				add(user);
				
			}

		}finally {
			if (con != null) {
				con.close();
			}
		}		
		return exist;
	}

	public int add(User user) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		int pk = -1;
		try {
			con = getConnection();

			if (user.getUserWeb() != null && user.getUserWeb().isEmpty()) {
				throw new SQLException("This user does not have any access collection for any website. Please add it");
			}

			pk = con.buildAddQuery(User.TBL_NAME)
					.setParameter(User.EMAIL, user.getEmail())
					.setParameter(User.FIRST_NAME, user.getFirstName())
					.setParameter(User.LAST_LOGIN, user.getLastLogin())
					.setParameter(User.LAST_NAME, user.getLastName())
					.setParameter(User.NBR_TRIES, user.getNbOfTries())
					.setParameter(User.PASSWORD, user.getPassword())
					.setParameter(User.USER_NAME, user.getUserName())
					.add();

			if (user.getUserWeb() != null && !user.getUserWeb().isEmpty()) {
				for(UserWeb uw : user.getUserWeb()) {
					con.buildAddQuery(UserWeb.TBL_NAME)
					.setParameter(UserWeb.USER_ID, pk)
					.setParameter(UserWeb.ACCESS_LEVEL, uw.getAccessLevel().name())
					.setParameter(UserWeb.WEBSITES, uw.getWebsiteAccess().name())
					.add();
				}
			}
		}finally {
			con.close();
		}		
		return pk;
	}	
//	public void addUserToken(int userId, String token) {
//		//TODO add token to user
//	}

	public User loadUserByUserNameForTokenValidation(String userName) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		User user = null;
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT " +User.ID + ", " + User.AUTH_TOKEN + ", " + User.USER_NAME +
					" FROM " + User.TBL_NAME + " where " + User.USER_NAME + " = :userName")
					.setParameter("userName", userName)
					.getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {
					user  = new User();
					user.setId(rs.getInt(User.ID));
					user.setAuthToken(rs.getString(User.AUTH_TOKEN));
					user.setUserName(rs.getString(User.USER_NAME));

					List<UserWeb> uwList = getUserWebForId(user.getId(), con);
					user.setUserWeb(uwList);

				}
			}
		}finally {
			con.close();
		}

		return user;
	}

	public void update(User user) throws ClassNotFoundException, SQLException {
		DBConnection con = null;
		try {
			con = getConnection();

			int upd = con.buildUpdateQuery(User.TBL_NAME)
					.setParameter(User.EMAIL, user.getEmail())
					.setParameter(User.FIRST_NAME, user.getFirstName())
					.setParameter(User.LAST_LOGIN, user.getLastLogin())
					.setParameter(User.LAST_NAME, user.getLastName())
					.setParameter(User.NBR_TRIES, user.getNbOfTries())
					.setParameter(User.PASSWORD, user.getPassword())
					.setParameter(User.USER_NAME, user.getUserName())
					.addUpdWhereClause("Where "+User.ID+" = :idValue", user.getId()).update();

			if (upd < 1) {
				throw new SQLException("Error updating User. " + upd);
			}

		}finally {
			con.close();
		}
	}
	public void updatePassword(int userId, String password) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		try {
			con = getConnection();

			int upd = con.buildUpdateQuery(User.TBL_NAME)
					
					.setParameter(User.PASSWORD, password)
					
					.addUpdWhereClause("Where "+User.ID+" = :idValue", userId).update();

			if (upd < 1) {
				throw new SQLException("Error updating User passwordr. " + upd);
			}

		}finally {
			con.close();
		}
	}
	/**
	 * load all user based on provided fields.. You can basically load all or just selected few..
	 * If fields is null, return all..
	 * 
	 * @param withAccess
	 * @param fields
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public List<User> loadAllUsers(boolean withAccess, String... fields) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		List<User> users = new ArrayList<User>();
		try {
			con = getConnection();

			StringBuilder query;

			if (fields != null && fields.length > 0) {
				query = new StringBuilder("Select ");
				boolean first = true;
				for(String s : fields) {
					if (first) {
						first = false;
						query.append(s);
					}else {
						query.append(" ,"+ s);
					}				
				}

				query.append(" FROM " + User.TBL_NAME);
			}else {
				query = new StringBuilder("Select * FROM " + User.TBL_NAME );
			}

			ResultSet rs = con.createSelectQuery(query.toString()).getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {
					User user  = new User(rs);

					if (withAccess) {
						List<UserWeb> uwList = getUserWebForId(user.getId(), con);
						user.setUserWeb(uwList);
					}

					users.add(user);
				}
			}
		}finally {
			con.close();
		}

		return users;
	}
	public User findByUserName(String userName, boolean access) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		User user = null;
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + User.TBL_NAME + " where " + User.USER_NAME + " = :userName" )
					.setParameter("userName", userName)
					.getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {
					user  = new User(rs);

					if (access) {
						List<UserWeb> uwList = getUserWebForId(user.getId(), con);
						user.setUserWeb(uwList);
					}
				}
			}
		}finally {
			if (con != null)
				con.close();
		}

		return user;
	}
	/**
	 * Get user for token validation.
	 * 
	 * @param userName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public User findUserForValidation(String userName) throws ClassNotFoundException, SQLException {
		DBConnection con = null;
		User user = null;
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT "+User.ID +", "+User.USER_NAME+" FROM " + User.TBL_NAME + " where " + User.USER_NAME + " = :userName" )
					.setParameter("userName", userName)
					.getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {
					user  = new User();
					user.setId(rs.getInt(User.ID));
					user.setUserName(rs.getString(User.USER_NAME));


					List<UserWeb> uwList = getUserWebForId(user.getId(), con);
					user.setUserWeb(uwList);	
				}
			}
		}finally {
			if (con != null)
				con.close();
		}

		return user;
	}

	public void deleteUser(int userId) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try {
			con = getConnection();

			String delete = "DELETE FROM " + User.TBL_NAME + " where " + User.ID+ " = :userId";
			con.createSelectQuery(delete)
			.setParameter("userId", userId).delete();

			//also delete UserWeb access
			String deleteUserWeb = "DELETE FROM " + UserWeb.TBL_NAME + " where " + UserWeb.USER_ID + " = :userId";
			con.createSelectQuery(deleteUserWeb)
			.setParameter("userId", userId).delete();


		}finally {
			con.close();
		}

	}

	public List<UserWeb> fetchAccessForUser(int userId) throws SQLException, ClassNotFoundException{
		DBConnection con = null;

		List<UserWeb> uwList = new ArrayList<UserWeb>();
		try {
			con = getConnection();

			uwList =  getUserWebForId(userId, con);
		}finally {
			con.close();
		}

		return uwList;
	}
	public void addUserWeb(List<UserWeb> uwList) throws SQLException, ClassNotFoundException {

		DBConnection con = null;
		try {
			con = getConnection();

			con.buildAddQuery(UserWeb.TBL_NAME);

			for(UserWeb uw: uwList){
				con.setParameter(UserWeb.ACCESS_LEVEL, uw.getAccessLevel().name())
				.setParameter(UserWeb.USER_ID, uw.getUserId())
				.setParameter(UserWeb.WEBSITES, uw.getWebsiteAccess().name())
				.addToBatch();
			}

			con.executeBatchQuery();

		}finally {
			con.close();
		}
	}
	public void updateUserWeb(List<UserWeb> uwList) throws ClassNotFoundException, SQLException {
		DBConnection con = null;
		try {
			con = getConnection();


			for(UserWeb uw: uwList){

				int upd = con.buildUpdateQuery(UserWeb.TBL_NAME)
						.setParameter(UserWeb.ACCESS_LEVEL, uw.getAccessLevel().name())
						.setParameter(UserWeb.WEBSITES, uw.getWebsiteAccess().name())
						.addUpdWhereClause("Where "+UserWeb.ID+" = :idValue", uw.getId()).update();
			}

		}finally {
			con.close();
		}
	}
	public void deleteUserWeb(List<UserWeb> uwList) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		try {
			con = getConnection();

			//			DELETE FROM table WHERE id IN (?,?,?,?,?,?,?,?)
			List<Object> intg = new ArrayList<Object>();
			for(UserWeb w : uwList) {
				intg.add(w.getId());
			}

			con.deleteInBatch(UserWeb.TBL_NAME, UserWeb.ID, intg);

		}finally {
			con.close();
		}
	}
	
	public User loadUserById(int id, boolean withAccess) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		User user = null;
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + User.TBL_NAME + " where " + User.ID + " = :userId" )
					.setParameter("userId", id)
					.getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {
					user  = new User(rs);

					if (withAccess) {
						List<UserWeb> uwList = getUserWebForId(user.getId(), con);
						user.setUserWeb(uwList);
					}
				}
			}
		}finally {
			if (con != null)
				con.close();
		}

		return user;
		
	}

	private List<UserWeb> getUserWebForId(int id, DBConnection con) throws SQLException{
		ResultSet rs2 = con.createSelectQuery("SELECT * FROM " + UserWeb.TBL_NAME + " Where " + UserWeb.USER_ID + " = :userId")
				.setParameter("userId", id)
				.getSelectResultSet();
		List<UserWeb> uwList = new ArrayList<UserWeb>();
		while(rs2.next()) {
			UserWeb uw = new UserWeb(rs2);
			uwList.add(uw);
		}
		return uwList;
	}
	private DBConnection getConnection() throws ClassNotFoundException, SQLException{

//		Database db = new Database("jdbc:h2:" +Constants.DB_URL,Constants.DB_USER, Constants.DB_PASS.toCharArray(), DbClass.Mysql);
		Database db = new Database(Constants.DB_MYSQL,Constants.DB_USER, Constants.DB_PASS, DbClass.Mysql);
//				return  new DBConnection(Constants.DB_MYSQL, Constants.DB_USER, Constants.DB_PASS, DbClass.Mysql );
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
