package net.web.db.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryGroup {

	//Table name
	public static String TBL_NAME 	= "inventory_group_tbl";

	//columns for user
	public static final String ID 			= "id";
	public static final String GROUP_NAME 	= "name";
	public static final String OWNER 		= "owner_id";
	public static final String NBR_ITEMS 	= "number_of_items"; 

	private int id 				= -1;
	private String groupName 	= "";		
	private int numberOfItems 	= 0;	
	private int ownerId 		= 0;

	public InventoryGroup(){}

	public InventoryGroup(ResultSet rs) throws SQLException{
		this.id 		= rs.getInt(ID);
		this.groupName 		= rs.getString(GROUP_NAME);
		this.numberOfItems 		= rs.getInt(NBR_ITEMS);
		this.ownerId 	= rs.getInt(OWNER);			

	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public int getNumberOfItems() {
		return numberOfItems;
	}
	public void setNumberOfItems(int numberOfItems) {
		this.numberOfItems = numberOfItems;
	}
	public int getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public static String createTable(){
		StringBuilder create = new StringBuilder();
		create.append("CREATE TABLE " + TBL_NAME + " (");
		create.append(ID + " INT PRIMARY KEY auto_increment");
		create.append(", " + GROUP_NAME + " VARCHAR(20)" );
		create.append(", " + NBR_ITEMS + " INT" ); 
		create.append(", " + OWNER + " INT" ); 
		create.append(")");

		return create.toString();
	}

	@Override
	public String toString() {
		return "InventoryGroup [id=" + id + ", groupName=" + groupName + ", numberOfItems=" + numberOfItems
				+ ", ownerId=" + ownerId + "]";
	}

}
