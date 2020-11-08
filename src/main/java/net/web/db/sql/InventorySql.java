package net.web.db.sql;

import home.db.ColumnType;
import home.db.DBConnection;
import home.db.Database;
import home.db.DbClass;
import home.db.PkCriteria;
import net.web.common.Constants;
import net.web.db.entity.Inventory;
import net.web.db.entity.InventoryGroup;
import net.web.db.entity.InventoryRef;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class InventorySql {

	private static final Logger logger = LogManager.getLogger(InventorySql.class);

	public void createTables() throws SQLException, ClassNotFoundException, IOException {
		logger.info("CreateUserTable");

		DBConnection con = null;
		boolean exist = false;
		try {
			con = getConnection();

			exist = con.verifyIfTableExist(Inventory.TBL_NAME);	

			logger.debug("Inventory table exist: " +  exist);
			if (!exist) {
				logger.info("Inventory table does not exist , creating");
				List<ColumnType> columns = new ArrayList<ColumnType>();					
				columns.add(new ColumnType(Inventory.ID, true).INT().setPKCriteria(new PkCriteria().autoIncrement()));
				columns.add(new ColumnType(Inventory.NAME, false).VarChar(200));
				columns.add(new ColumnType(Inventory.QTY, false).INT());
				columns.add(new ColumnType(Inventory.CATEGORY, false).VarChar(500));
				columns.add(new ColumnType(Inventory.DETAILS, false).VarChar(3000));
				columns.add(new ColumnType(Inventory.THUMB_NAME, false).VarChar(10000));
				columns.add(new ColumnType(Inventory.GROUP_ID, false).INT());
				columns.add(new ColumnType(Inventory.OWNER, false).INT());
				con.createTable(Inventory.TBL_NAME, columns);	
			}
			exist = false;
			exist = con.verifyIfTableExist(InventoryRef.TBL_NAME);
			
			logger.debug("Inventory reference table exist: " +  exist);
			if (!exist) {

				List<ColumnType> columns = new ArrayList<ColumnType>();					
				columns.add(new ColumnType(InventoryRef.ID, true).INT().setPKCriteria(new PkCriteria().autoIncrement()));
				columns.add(new ColumnType(InventoryRef.REF_NAME, false).VarChar(200));
				columns.add(new ColumnType(InventoryRef.REF_TYPE, false).VarChar(20));
				columns.add(new ColumnType(InventoryRef.INVENTORY_ID_FK, false).INT());
				
				con.createTable(InventoryRef.TBL_NAME, columns);	
			}
			exist = false;
			exist = con.verifyIfTableExist(InventoryGroup.TBL_NAME);
			
			logger.debug("Inventory group table exist: " +  exist);
			if (!exist) {
				List<ColumnType> columns = new ArrayList<ColumnType>();					
				columns.add(new ColumnType(InventoryGroup.ID, true).INT().setPKCriteria(new PkCriteria().autoIncrement()));
				columns.add(new ColumnType(InventoryGroup.GROUP_NAME, false).VarChar(30));
				columns.add(new ColumnType(InventoryGroup.NBR_ITEMS, false).INT());
				columns.add(new ColumnType(InventoryGroup.OWNER, false).INT());
				
				con.createTable(InventoryGroup.TBL_NAME, columns);	
			}
		}finally {
			if (con != null) {
				con.close();
			}
		}
	}
	/**
	 * load all inventory for the current user.  //TODO load shared in the future,.
	 * 
	 * @param userId
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public List<Inventory> loadAllInventoryForUser(int userId, boolean loadGroup) throws SQLException, ClassNotFoundException{
		List<Inventory> invList = new ArrayList<Inventory>();

		Map<Integer, InventoryGroup> groups = new HashMap<Integer, InventoryGroup>();

		DBConnection con = null;
		try{
			con = getConnection();

			String query = "SELECT * FROM " + Inventory.TBL_NAME + " where " + Inventory.OWNER + " = :owner";

			ResultSet rs = con.createSelectQuery(query)
					.setParameter("owner", userId)
					.getSelectResultSet();

			while (rs.next()) {
				Inventory inv = new Inventory(rs);

				//load groups 
				if (loadGroup){
					if(groups.containsKey(inv.getGroupId())){
						inv.setGroup(groups.get(inv.getGroupId()));
					}else{
						//load from db
						String queryGroup = "SELECT * FROM " + InventoryGroup.TBL_NAME + " where " + InventoryGroup.ID + " = :groupId";
						ResultSet rsGroup = con.createSelectQuery(queryGroup)
								.setParameter("groupId", inv.getGroupId())
								.getSelectResultSet();

						while (rsGroup.next()) {
							InventoryGroup ig = new InventoryGroup(rsGroup);
							inv.setGroup(ig);
							groups.put(inv.getGroupId(), ig);
						}
					}
				}

				//load references to inventory item
				inv.setReferences(loadReferences(con, inv.getId()));


				invList.add(inv);
			}

		}finally{
			if (con!=null){
				con.close();
			}
		}
		return invList;
	}

	public List<Inventory> loadAllInventoryByUserAndGroup(int userId, int groupId) throws SQLException, ClassNotFoundException{
		List<Inventory> invList = new ArrayList<Inventory>();
		DBConnection con = null;
		try{
			con = getConnection();

			String query = "SELECT * FROM " + Inventory.TBL_NAME + " where " + Inventory.OWNER + " = :owner AND " + Inventory.GROUP_ID + " = :group";

			ResultSet rs = con.createSelectQuery(query)
					.setParameter("owner", userId)
					.setParameter("group", groupId)
					.getSelectResultSet();

			while (rs.next()) {
				Inventory inv = new Inventory(rs);

				//load all inventory item references
				inv.setReferences(loadReferences(con, inv.getId()));

				invList.add(inv);
			}

		}finally{
			if (con!=null){
				con.close();
			}
		}
		return invList;
	}

	/**
	 * Load the group names for the side menu.
	 * @param userId
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public List<InventoryGroup> loadSideMenuGroupsForUser(int userId) throws SQLException, ClassNotFoundException{
		List<InventoryGroup> groupList = new ArrayList<InventoryGroup>();
		DBConnection con = null;
		try{
			con = getConnection();

			String query = "SELECT * FROM " + InventoryGroup.TBL_NAME + " where " + InventoryGroup.OWNER + " = :owner";

			ResultSet rs = con.createSelectQuery(query)
					.setParameter("owner", userId)
					.getSelectResultSet();

			while (rs.next()) {
				InventoryGroup groupName = new InventoryGroup(rs);
				groupList.add(groupName);
			}

		}finally{
			if (con!=null){
				con.close();
			}
		}
		return groupList;
	}
	/**
	 * Load the inventory group by the group ID.
	 * @param grpId
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public InventoryGroup loadInvetoryGroupById(int grpId) throws SQLException, ClassNotFoundException{
		InventoryGroup group = null;
		DBConnection con = null;
		try{
			con = getConnection();

			String query = "SELECT * FROM " + InventoryGroup.TBL_NAME + " where " + InventoryGroup.ID + " = :id";

			ResultSet rs = con.createSelectQuery(query)
					.setParameter("id", grpId)
					.getSelectResultSet();

			while (rs.next()) {
				group = new InventoryGroup(rs);

			}

		}finally{
			if (con!=null){
				con.close();
			}
		}
		return group;
	}
	/**
	 * Load inventory item by id
	 * @param id
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public Inventory loadInventoryById(int id) throws SQLException, ClassNotFoundException{
		Inventory inv = null;
		DBConnection con = null;
		try{
			con = getConnection();

			String query = "SELECT * FROM " + Inventory.TBL_NAME + " where " + Inventory.ID + " = :id";

			ResultSet rs = con.createSelectQuery(query)
					.setParameter("id", id)
					.getSelectResultSet();

			while (rs.next()) {
				inv = new Inventory(rs);
				//load references								
				inv.setReferences(loadReferences(con, inv.getId()));		

			}

		}finally{
			if (con!=null){
				con.close();
			}
		}
		return inv;
	}

	public Inventory addInventoryItem(Inventory item) throws SQLException, ClassNotFoundException{
		DBConnection con = null;

		try{
			con = getConnection();

			int key = con.buildAddQuery(Inventory.TBL_NAME)
					.setParameter(Inventory.NAME, item.getName())
					.setParameter(Inventory.CATEGORY, item.getCategory())
					.setParameter(Inventory.OWNER, item.getOwnerId())
					.setParameter(Inventory.QTY, item.getQty())
					.setParameter(Inventory.GROUP_ID, item.getGroupId())
					.setParameter(Inventory.DETAILS, item.getDetails())
					.setParameter(Inventory.THUMB_NAME, item.getThumbBase64())
					.add();

			item.setId(key);

			//add the references to the item.
			if (item.getReferences() != null && !item.getReferences().isEmpty()){
				addReference(con, item.getReferences(), key);
			}			
		}finally{
			if (con!=null){
				con.close();
			}
		}
		return item;
	}

	public void updateInventoryItem(Inventory item) throws SQLException, ClassNotFoundException{
		DBConnection con = null;
		try{
			con = getConnection();

			con.buildUpdateQuery(Inventory.TBL_NAME)

			.setParameter(Inventory.CATEGORY, item.getCategory())
			.setParameter(Inventory.DETAILS, item.getDetails())
			.setParameter(Inventory.GROUP_ID, item.getGroupId())
			.setParameter(Inventory.NAME, item.getName())
			.setParameter(Inventory.OWNER, item.getOwnerId())
			.setParameter(Inventory.QTY, item.getQty())
			.setParameter(Inventory.THUMB_NAME, item.getThumbBase64())
			.addUpdWhereClause("WHERE " + Inventory.ID + " = :invId", item.getId())
			.update();


		}finally{
			if (con!=null){
				con.close();
			}
		}
	}

	public InventoryGroup addInvGroup(InventoryGroup ig) throws SQLException, ClassNotFoundException{
		DBConnection con = null;

		try{
			con = getConnection();

			int key = con.buildAddQuery(InventoryGroup.TBL_NAME)
					.setParameter(InventoryGroup.GROUP_NAME, ig.getGroupName())
					.setParameter(InventoryGroup.NBR_ITEMS, ig.getNumberOfItems())
					.setParameter(InventoryGroup.OWNER, ig.getOwnerId())
					.add();

			ig.setId(key);
		}finally{
			if (con!=null){
				con.close();
			}
		}

		return ig;
	}

	public void deleteItem(int itemId) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = getConnection();

			String query = "DELETE FROM " + Inventory.TBL_NAME + " where " + Inventory.ID + " = :id";

			con.createSelectQuery(query)
			.setParameter("id", itemId)
			.delete();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deleteListOfItem(List<Integer> itemIds) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = getConnection();

			con.deleteInBatch(Inventory.TBL_NAME, Inventory.ID, (List)itemIds);

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}

	public void deleteGroup(InventoryGroup ig) throws SQLException, ClassNotFoundException{

		logger.debug("Deleting group: " + ig);
		DBConnection con = null;
		try{
			con = getConnection();

			String query = "DELETE FROM " + InventoryGroup.TBL_NAME + " where " + InventoryGroup.ID + " = :id";

			 con.createSelectQuery(query)
			.setParameter("id", ig.getId())
			.delete();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deleteListOfReferences(List<Integer> refIds) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = getConnection();

			con.deleteInBatch(InventoryRef.TBL_NAME, InventoryRef.ID, (List)refIds);

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}	

	/**
	 * add the inventory references to the table.
	 * @param con
	 * @param refs
	 * @param invId
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public void addReference(DBConnection con, List<InventoryRef> refs, int invId) throws SQLException, ClassNotFoundException{

		boolean closeCon = false;
		if (con == null){
			con = getConnection();
			closeCon = true;
		}

		con.buildAddQuery(InventoryRef.TBL_NAME);

		for(InventoryRef ref : refs){		 
			con.setParameter(InventoryRef.REF_NAME, ref.getReferenceName())
			.setParameter(InventoryRef.REF_TYPE, ref.getType().name())
			.setParameter(InventoryRef.INVENTORY_ID_FK, invId)				
			.addToBatch();
		}

		con.executeBatchQuery();

		if (closeCon){
			con.close();
		}

	}
	public void updateReference(DBConnection con, List<InventoryRef> refs, int invId) throws SQLException, ClassNotFoundException{

		boolean closeCon = false;
		if (con == null){
			con = getConnection();
			closeCon = true;
		}		

		for(InventoryRef ref : refs){	

			con.buildUpdateQuery(InventoryRef.TBL_NAME)

			.setParameter(InventoryRef.REF_NAME, ref.getReferenceName())
			.setParameter(InventoryRef.REF_TYPE, ref.getType().name())
			.setParameter(InventoryRef.INVENTORY_ID_FK, invId)

			.addUpdWhereClause("WHERE " + InventoryRef.ID + " = :invId", ref.getId())
			.update();
		}

		if (closeCon){
			con.close();
		}	
	}

	public List<InventoryRef> loadReferences(DBConnection con, int invId) throws SQLException, ClassNotFoundException{

		boolean closeCon = false;
		if (con == null){
			con = getConnection();
			closeCon = true;
		}		

		String refQuery = "Select * from " + InventoryRef.TBL_NAME + " WHERE " + InventoryRef.INVENTORY_ID_FK + " = :itemId";
		ResultSet refRs = con.createSelectQuery(refQuery).setParameter("itemId", invId).getSelectResultSet();

		List<InventoryRef> refs = new ArrayList<InventoryRef>();
		while(refRs.next()){
			InventoryRef ref = new InventoryRef(refRs);
			refs.add(ref);
		}

		if (closeCon){
			con.close();
		}

		return refs;
	}

	public void updateInvGroup(InventoryGroup grp) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = getConnection();

			con.buildUpdateQuery(InventoryGroup.TBL_NAME)
			.setParameter(InventoryGroup.GROUP_NAME, grp.getGroupName())
			.setParameter(InventoryGroup.NBR_ITEMS, grp.getNumberOfItems())
			.addUpdWhereClause("WHERE " + InventoryGroup.ID + " = :invId", grp.getId())
			.update();


		}finally{
			if (con!=null){
				con.close();
			}
		}

	}
	
	public List<Inventory> search(String str) throws SQLException, ClassNotFoundException{ 
		
		DBConnection con = null;
		List<Inventory> invList = new ArrayList<>();

		Map<Integer, List<InventoryRef>> inventoryRefMap = new HashMap<>();
		Map<Integer, Inventory> inventoryMap = new TreeMap<>(); // Treeset	
		try{
			
					
			con = getConnection();			

			String query = "SELECT * FROM " + Inventory.TBL_NAME + " AS inv left join " + InventoryRef.TBL_NAME + " AS invRef on invRef." + InventoryRef.INVENTORY_ID_FK + " = " +
							"inv." + Inventory.ID + " where "
									+ "inv."+Inventory.NAME +" LIKE '%" + str + "%' OR "
									+ "inv."+Inventory.CATEGORY +" LIKE '%" + str + "%' OR "
									+ "inv."+Inventory.DETAILS +" LIKE '%" + str + "%' OR "
									+ "inv."+Inventory.QTY +" LIKE '%" + str + "%' OR "
									+ "invRef."+InventoryRef.REF_NAME +" LIKE '%" + str + "%' ";

//			System.out.println("Query!!!!!!!!!!!!! : " + query);
			
			ResultSet rs = con.createSelectQuery(query)
					.getSelectResultSet();

			while (rs.next()) {
			
				Integer id = rs.getInt(Inventory.ID);
				Inventory inv = inventoryMap.get(id);
				
				if (inv == null) {
					inv = new Inventory(rs);
					
					inventoryMap.put(id, inv);
					inventoryRefMap.put(id, new ArrayList<>());
				}
				
				String refName = rs.getString(InventoryRef.REF_NAME);
//				System.out.println("refName: " + refName);
				 
				if (!rs.wasNull()) {
					List<InventoryRef> invRefList = inventoryRefMap.get(id);
					InventoryRef invRef = new InventoryRef(rs);
					invRefList.add(invRef);
									
				}

			}

		}finally{
			if (con!=null){
				con.close();
			}
		}
		//add to list
		for(Inventory i : inventoryMap.values()) {
			i.setReferences(inventoryRefMap.get(i.getId()));
			invList.add(i);
		}
		
		
		return invList;

	}
	
	private DBConnection getConnection() throws ClassNotFoundException, SQLException{

		Database db = new Database(Constants.DB_MYSQL,Constants.DB_USER, Constants.DB_PASS, DbClass.Mysql);
		//				return  new DBConnection(Constants.DB_MYSQL, Constants.DB_USER, Constants.DB_PASS, DbClass.Mysql );
		return new DBConnection(db);
	}
}
