package net.web.db.entity;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.web.enums.AccessLevel;
import net.web.enums.Website;

public class User {

	//Table name
	public static String TBL_NAME 	= "usertbl";
	
	//columns for user
	public static final String ID 			= "id";
	public static final String USER_NAME 	= "username";
	public static final String PASSWORD 	= "password";
	public static final String FIRST_NAME 	= "firstname";
	public static final String LAST_NAME 	= "lastname";
	public static final String EMAIL 		= "email";
	public static final String LAST_LOGIN 	= "lastlogin";
	public static final String NBR_TRIES 	= "nbrtries";
	public static final String AUTH_TOKEN 	= "authToken";
	
	//DB fields
	private int    id			= -1;
	private String userName 	= "";
	private String password 	= "";
	private String firstName	= "";
	private String lastName 	= "";
	private String email 		= "";
	private Date lastLogin;
	private int nbOfTries 		= 0;
	private String authToken	= "";
	
	
	private List<UserWeb> userWeb = new ArrayList<UserWeb>();
	
	//variable to keep current website access.. set in filter not stored in DB
	private Website currentWebsite ;
	private String  access = ""; // Current access level to tell to the UI.. 

	public User(){}
	public User(ResultSet rs) throws SQLException{

		ResultSetMetaData rsmd = rs.getMetaData();
		for(int i = 1 ; i <= rsmd.getColumnCount()  ; i++){

			if (ID.equalsIgnoreCase(rsmd.getColumnName(i))){
				id 		= rs.getInt(ID);	
			}else if (USER_NAME.equalsIgnoreCase(rsmd.getColumnName(i))){
				userName 	= rs.getString(USER_NAME);
			}else if (PASSWORD.equalsIgnoreCase(rsmd.getColumnName(i))){
				password 	= rs.getString(PASSWORD);
			}else if (FIRST_NAME.equalsIgnoreCase(rsmd.getColumnName(i))){
				firstName	= rs.getString(FIRST_NAME);
			}else if (LAST_NAME.equalsIgnoreCase(rsmd.getColumnName(i))){
				lastName 	= rs.getString(LAST_NAME);
			}else if (EMAIL.equalsIgnoreCase(rsmd.getColumnName(i))){
				email 		= rs.getString(EMAIL);
			}else if (LAST_LOGIN.equalsIgnoreCase(rsmd.getColumnName(i))){
				lastLogin 	= rs.getTimestamp(LAST_LOGIN);
			}else if (NBR_TRIES.equalsIgnoreCase(rsmd.getColumnName(i))){
				nbOfTries 	= rs.getInt(NBR_TRIES);
			}else if (AUTH_TOKEN.equalsIgnoreCase(rsmd.getColumnName(i))){
				this.authToken = rs.getString(AUTH_TOKEN);
			}
			
			
		}
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Date getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	public int getNbOfTries() {
		return nbOfTries;
	}
	public void setNbOfTries(int nbOfTries) {
		this.nbOfTries = nbOfTries;
	}
	public String fullName(){
		String fullName = "";
		if (this.firstName != null && this.firstName.trim().length() > 0){
			fullName += this.firstName + " ";
		}
		if (this.lastName != null && this.lastName.trim().length() > 0){
			fullName += this.lastName ;
		}
		return fullName;
	}
	/**
	 * this tells you that he has access to the website, but not the user level.
	 * @param web
	 * @return
	 */
	public boolean hasWebsiteAccess(Website web) {
		if (web == null) {
			return false;
		}else {
			return userWeb.stream().anyMatch(wa -> wa.getWebsiteAccess() == web);//.findAny();
		}
		
	}
	/**
	 * This method return true if the user has the access requested.
	 * ex: if Method ABC needs regular for SERVICE_WEBSITE, the method will return true if the user has those access.
	 * 
	 * Level weight is only a pattern that if an regular access is required and the user is an adminstrator.. then he has access
	 * since he is a higher level than regular access.. 
	 * 
	 * So if the user is an admin and the access requested is regular, the user will have access.
	 * 
	 * @param level
	 * @param web
	 * @return
	 */
	public boolean hasPermission(AccessLevel level, Website web) {
		//loop through until the highest permission is found.
		//Admin
		//regular / editor
		//view
		
		
		return userWeb.stream().anyMatch(u -> u.getWebsiteAccess() == web && 
				( u.getAccessLevel() == level || u.getAccessLevel().getLevelWeight() >= level.getLevelWeight()) );

		
	}
	
	/**
	 * Determin if the user can modifiy the controls (Turn on or off)
	 * @return
	 */
	public boolean canModify(){
		boolean canMod = false;
		
//		if (getAccess() == AccessLevel.REGULAR || getAccess() == AccessLevel.ADMIN){
//			canMod = true;
//		}
		return canMod;
	}
	public static String checkIfTableExist() { 
		return "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ='"+TBL_NAME+"'"; 
	}
	
	
	public List<UserWeb> getUserWeb() {
		return userWeb;
	}
	public void setUserWeb(List<UserWeb> userWeb) {
		this.userWeb = userWeb;
	}
	public String getAuthToken() {
		return authToken;
	}
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
	public Website getCurrentWebsite() {
		return currentWebsite;
	}
	public void setCurrentWebsite(Website currentWebsite) {
		this.currentWebsite = currentWebsite;
	}
	public String getAccess() {
		return access;
	}
	public void setAccess(String access) {
		this.access = access;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("User [id=");
		builder.append(id);
		builder.append(", userName=");
		builder.append(userName);
		builder.append(", password=");
		builder.append(password);
		builder.append(", firstName=");
		builder.append(firstName);
		builder.append(", lastName=");
		builder.append(lastName);
		builder.append(", email=");
		builder.append(email);
		builder.append(", lastLogin=");
		builder.append(lastLogin);
		builder.append(", nbOfTries=");
		builder.append(nbOfTries);
		builder.append(", token=");
		builder.append(authToken);
		if (this.userWeb != null && !this.userWeb.isEmpty()) {
			builder.append("  Access: ");
			builder.append(this.userWeb);
		}
			
		builder.append("]");
		return builder.toString();
	}
}
