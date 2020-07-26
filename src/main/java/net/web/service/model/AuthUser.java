package net.web.service.model;

public class AuthUser {

	private String userName = "";
	private String password = "";
	private String websiteToAccess = "";
	
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
	public String getWebsiteToAccess() {
		return websiteToAccess;
	}
	public void setWebsiteToAccess(String websiteToAccess) {
		this.websiteToAccess = websiteToAccess;
	}
	@Override
	public String toString() {
		return "AuthUser [userName=" + userName + ", password=" + password + ", websiteToAccess=" + websiteToAccess
				+ "]";
	}
	
	
}
