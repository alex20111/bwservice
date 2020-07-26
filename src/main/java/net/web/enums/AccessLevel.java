package net.web.enums;

public enum AccessLevel {
	UNAUTHORIZED("unauthorized", 0), VIEW("View", 10),REGULAR("Regular", 50),ADMIN("Administrator", 99);
	
	private String access;
	private int levelWeight = -1;
	
	private AccessLevel(String access, int levelWeight){
		this.access = access;
		this.levelWeight = levelWeight;
	}
	
	public String getAccess(){
		return this.access;
	}
	
	public int getLevelWeight() {
		return levelWeight;
	}
}
