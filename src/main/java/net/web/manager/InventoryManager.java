package net.web.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.web.db.entity.Inventory;
import net.web.db.entity.InventoryGroup;
import net.web.db.entity.InventoryRef;
import net.web.db.sql.InventorySql;
import net.web.exception.ValidationException;


public class InventoryManager {

	private InventorySql sql;
	
	public InventoryManager(){
		this.sql = new InventorySql();
	}

	/**
	 * Load all inventory items for all groups 
	 * @param userId
	 * @param loadGroup
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public List<Inventory> loadAllInventoryForUser(int userId, boolean loadGroup) throws SQLException, ClassNotFoundException{
		return sql.loadAllInventoryForUser(userId, loadGroup);
	}
	
	public List<Inventory> loadAllInventoryByUserAndGroup(int userId, int groupId) throws SQLException, ClassNotFoundException{
		return sql.loadAllInventoryByUserAndGroup(userId, groupId);
	}
	
	public Inventory add(Inventory item) throws ClassNotFoundException, SQLException{

		//clean up empty references
		if (item.getReferences() != null && !item.getReferences().isEmpty()){
			
			for (Iterator<InventoryRef> iter = item.getReferences().listIterator(); iter.hasNext(); ) {
				InventoryRef ref = iter.next();
				if (ref == null || ref.getReferenceName() == null || ref.getReferenceName().trim().length() == 0){
					iter.remove();
				}
			}
		}		

		return sql.addInventoryItem(item);
	}

	public void update(Inventory item) throws ClassNotFoundException, SQLException{

		//1st update the inventory
		sql.updateInventoryItem(item);

		//then update the references:
		if (item.getReferences() != null && !item.getReferences().isEmpty()){
			List<InventoryRef> toAdd = new ArrayList<InventoryRef>();
			List<InventoryRef> toUpd = new ArrayList<InventoryRef>();
			List<InventoryRef> toDel = new ArrayList<InventoryRef>();

			for(InventoryRef ref : item.getReferences()){
				if (ref.getId() > 0){
					if (ref.getReferenceName().trim().length() == 0){
						toDel.add(ref);
					}else{
						toUpd.add(ref);
					}
				}else if (ref.getId() == -1 && ref.getReferenceName().trim().length() > 0){
					toAdd.add(ref);
				}
			}
			
			if(!toAdd.isEmpty()){
				sql.addReference(null, toAdd, item.getId());
			}
			if(!toUpd.isEmpty()){
				sql.updateReference(null, toUpd, item.getId());
			}
			if(!toDel.isEmpty()){
				List<Integer> refIds = new ArrayList<Integer>();
				for(InventoryRef ref: toDel){
					refIds.add(ref.getId());				
				}		
				sql.deleteListOfReferences(refIds);			
			}			
		}
	}
	
	public List<InventoryGroup> loadSideMenuGroupsForUser(int userId) throws SQLException, ClassNotFoundException{
		List<InventoryGroup> groups =  sql.loadSideMenuGroupsForUser(userId);
		
		Collections.sort(groups, new Comparator<InventoryGroup>() {
		    @Override
		    public int compare(InventoryGroup s1, InventoryGroup s2) {
		        return s1.getGroupName().compareToIgnoreCase(s2.getGroupName());
		    }
		});
		
		return groups;
	}
	public InventoryGroup addInvGroup(InventoryGroup ig) throws SQLException, ClassNotFoundException, ValidationException{
		
		InventoryGroup newGroup = null;
		if (ig != null) {
			
			if (ig.getGroupName() != null && ig.getGroupName().trim().length() > 0) {
				
				newGroup = sql.addInvGroup(ig);
				
			}else {
				throw new ValidationException("Group name is empty");
			}			
		}else {
			throw new IllegalArgumentException("Inventory Group null");
		}
		return newGroup;
	}
	
	public Inventory loadInventoryById(int id) throws SQLException, ClassNotFoundException{
		return sql.loadInventoryById(id);
	}
	public InventoryGroup loadInvetoryGroupById(int grpId) throws SQLException, ClassNotFoundException{
		return sql.loadInvetoryGroupById(grpId);
	}
	
	public void deleteItem(int itemId) throws SQLException, ClassNotFoundException{
		
		//del references
		List<InventoryRef> refs = sql.loadReferences(null, itemId);
		if (!refs.isEmpty()){
			
			List<Integer> refIds = new ArrayList<Integer>();
			for(InventoryRef ref: refs){
				refIds.add(ref.getId());				
			}
			
			if (!refIds.isEmpty()){
				sql.deleteListOfReferences(refIds);
			}
		}	
		
		sql.deleteItem(itemId);
	}
	
	public void deleteGroup(int groupId, int userId) throws ClassNotFoundException, SQLException{
		//1st we must delete the items for that group
		List<Inventory> items = loadAllInventoryByUserAndGroup(userId, groupId);
		
		if (!items.isEmpty()){
			List<Integer> itemIds = new ArrayList<Integer>();
			for(Inventory i : items){
				itemIds.add(i.getId());
			}
			sql.deleteListOfItem(itemIds);

			//delete references
			if (!itemIds.isEmpty()){

				List<InventoryRef> refs = new ArrayList<InventoryRef>();

				//fetch all the reference ids of all items
				for(Integer itemId : itemIds){
					//del references
					refs.addAll(sql.loadReferences(null, itemId));						
				}

				//delete all
				if (!refs.isEmpty()){

					List<Integer> refIds = new ArrayList<Integer>();
					for(InventoryRef ref: refs){
						refIds.add(ref.getId());				
					}

					if (!refIds.isEmpty()){
						sql.deleteListOfReferences(refIds);
					}
				}
			}
		}
		
		InventoryGroup ig = new InventoryGroup();
		ig.setId(groupId);
		sql.deleteGroup(ig);	
		
		
	}
	
	public void updateInvGroup(InventoryGroup grp) throws ClassNotFoundException, SQLException{
		sql.updateInvGroup(grp);
	}
}
