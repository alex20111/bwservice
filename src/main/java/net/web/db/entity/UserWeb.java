package net.web.db.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.web.enums.AccessLevel;
import net.web.enums.Website;

/*
 * Class containing the access level for each websites.
 */
public class UserWeb {

	public static final String TBL_NAME		= "user_access_web";
	public static final String ID 			= "id";
	public static final String ACCESS_LEVEL	= "accessLevel";
	public static final String WEBSITES 	= "websites";
	public static final String USER_ID		= "userId";
	
	private int id = -1;
	private AccessLevel accessLevel;  //access level to that website
	private Website websiteAccess;
	private int userId = -1;
	
	public UserWeb() {}
	
	public UserWeb(ResultSet rs) throws SQLException {
		
		this.id = rs.getInt(ID);
		this.accessLevel = AccessLevel.valueOf(rs.getString(ACCESS_LEVEL));
		this.websiteAccess = Website.valueOf(rs.getString(WEBSITES));
		this.userId = rs.getInt(USER_ID);
		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public AccessLevel getAccessLevel() {
		return accessLevel;
	}
	public void setAccessLevel(AccessLevel accessLevel) {
		this.accessLevel = accessLevel;
	}
	public Website getWebsiteAccess() {
		return websiteAccess;
	}
	public void setWebsiteAccess(Website websiteAccess) {
		this.websiteAccess = websiteAccess;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public static String checkIfTableExist() { 
		return "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ='"+TBL_NAME+"'"; 
	}

	@Override
	public String toString() {
		return "UserWeb [id=" + id + ", accessLevel=" + accessLevel + ", websiteAccess=" + websiteAccess + ", userId="
				+ userId + "]";
	}
	
}
