package net.web.db.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Inventory {

	//thumbnail disk location
	//public static final String THUMB_LOC = "c:\\temp\\thumbs\\";
	
	//Table name
	public static String TBL_NAME 	= "inventorytbl";

	//columns for user
	public static final String ID 			= "id";
	public static final String NAME 		= "name";
	public static final String QTY	 		= "quantity";
	public static final String CATEGORY 	= "category"; //what is this item referring to. Resistor, pcb, led.		
	public static final String DETAILS 		= "details";
	public static final String THUMB_NAME	= "thumb_base64"; 
	public static final String GROUP_ID 		= "group_id"; //a group. Ex: electronic components. Pool items.
	public static final String OWNER 		= "owner_id"; //user that own the invetory group	


	private int id 			= -1;
	private String name 	= "";
	private int qty 		= 0;
	private String category = "";		
	private String details 	= "";
	private String thumbBase64 = "";
	private int groupId		= -1; //fk
	private int ownerId 	= -1; //fk
	
	//relation
	private InventoryGroup group = null;
	private List<InventoryRef> references;

	public Inventory(){}

	public Inventory(ResultSet rs) throws SQLException{
		this.id 		= rs.getInt(ID);
		this.name 		= rs.getString(NAME);
		this.qty 		= rs.getInt(QTY);
		this.category 	= rs.getString(CATEGORY);			
		this.details 	= rs.getString(DETAILS);
		this.thumbBase64  = rs.getString(THUMB_NAME);
		this.groupId 		= rs.getInt(GROUP_ID);
		this.ownerId 	= rs.getInt(OWNER);
		
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getQty() {
		return qty;
	}
	public void setQty(int qty) {
		this.qty = qty;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public int getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}
	
	public InventoryGroup getGroup() {
		return group;
	}
	public void setGroup(InventoryGroup group) {
		this.group = group;
	}
	public List<InventoryRef> getReferences() {
		return references;
	}
	public void setReferences(List<InventoryRef> references) {
		this.references = references;
	}
	public String getThumbBase64() {
		return thumbBase64;
	}
	public void setThumbBase64(String thumbBase64) {
		this.thumbBase64 = thumbBase64;
	}

	@Override
	public String toString() {
		return "Inventory [id=" + id + ", name=" + name + ", qty=" + qty
				+ ", category=" + category + ", details=" + details
				+ ", groupId=" + groupId + ", ownerId=" + ownerId
				+ "]";
	}

}
