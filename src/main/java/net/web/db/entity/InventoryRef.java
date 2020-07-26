package net.web.db.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.web.enums.RefType;


public class InventoryRef {
	//Table name
	public static String TBL_NAME 	= "inventory_ref_tbl";

	//columns for user
	public static final String ID 				= "id";
	public static final String REF_NAME 		= "reference_name";
	public static final String REF_TYPE 		= "reference_type";
	public static final String INVENTORY_ID_FK 	= "inventory_id";


	private int id 					= -1;
	private String referenceName 	= "";		
	private RefType type 			= RefType.text;	
	private int inventoryId 		= -1;

	public InventoryRef(){}

	public InventoryRef(ResultSet rs) throws SQLException{
		this.id 			= rs.getInt(ID);
		this.referenceName 	= rs.getString(REF_NAME);
		this.type 			= RefType.valueOf(rs.getString(REF_TYPE));
		this.inventoryId 	= rs.getInt(INVENTORY_ID_FK);	

	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getReferenceName() {
		return referenceName;
	}
	public void setReferenceName(String referenceName) {
		this.referenceName = referenceName;
	}
	public RefType getType() {
		return type;
	}
	public void setType(RefType type) {
		this.type = type;
	}
	public int getInventoryId() {
		return inventoryId;
	}
	public void setInventoryId(int inventoryId) {
		this.inventoryId = inventoryId;
	}
	
	public static String createTable(){
		StringBuilder create = new StringBuilder();
		create.append("CREATE TABLE " + TBL_NAME + " (");
		create.append(ID + " INT PRIMARY KEY auto_increment");
		create.append(", " + REF_NAME + " VARCHAR(200)" );
		create.append(", " + REF_TYPE + " VARCHAR(20)" ); 
		create.append(", " + INVENTORY_ID_FK + " INT" ); 
		create.append(")");

		return create.toString();
	}

	@Override
	public String toString() {
		return "InventoryRef [id=" + id + ", referenceName=" + referenceName
				+ ", type=" + type + ", inventoryId=" + inventoryId + "]";
	}
}
