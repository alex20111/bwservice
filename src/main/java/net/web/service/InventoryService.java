package net.web.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.web.common.Constants;
import net.web.db.entity.Inventory;
import net.web.db.entity.InventoryGroup;
import net.web.db.entity.User;
import net.web.enums.AccessLevel;
import net.web.exception.ValidationException;
import net.web.manager.InventoryManager;
import net.web.manager.UserManager;
import net.web.service.filter.TokenValidation;
import net.web.service.model.Message;

@Path("inv")
public class InventoryService {

	private static final Logger logger = LogManager.getLogger(InventoryService.class);

	@Context
	private transient HttpServletRequest servletRequest;

	/** 
	 * Get all inventory item for the user
	 * @return
	 */
	@Path("allInvItems")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response getAllInvItemsForUser() {

		logger.debug("Get all Inventory items for logged user ");

		Message msg = null;
		Status status = Status.FORBIDDEN;

		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username

			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite())) {
				logger.debug("fetch inventory groups");
				InventoryManager im = new InventoryManager();


				return Response.ok().entity(msg).build();
			}
			else {
				msg = new Message("No Access", "User does not have permission");
			}
		}catch(Exception ex) {
			logger.error("error in adding user" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}

		return Response.status(status).entity(msg).build();


	}

	/**
	 * Load all side menu items
	 */
	@Path("invGroup")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response getAllInventoryGroupForUser() {
		logger.debug("getAllSideMenuItemsForUser");

		Message msg = null;
		Status status = Status.FORBIDDEN;

		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username

			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite())) {
				logger.debug("fetch inventory groups");
				InventoryManager im = new InventoryManager();

				List<InventoryGroup> imSideMenu = im.loadSideMenuGroupsForUser(userReq.getId());

				return Response.ok().entity(imSideMenu).build();
			}
			else {
				msg = new Message("No Access", "User does not have permission");
			}
		}catch(Exception ex) {
			logger.error("error in adding user" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}

		return Response.status(status).entity(msg).build();

	}
	/**
	 * Add new group to the inventory
	 * 
	 * @return
	 */
	@Path("addGroup")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response addGroup(InventoryGroup invGrp) {
		logger.debug("addGroup: " + invGrp);

		Message msg = null;
		Status status = Status.FORBIDDEN;
		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username
			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite())) {
				logger.debug("Has access.. adding group");
				
				InventoryManager im = new InventoryManager();
				invGrp.setOwnerId(userReq.getId());
				InventoryGroup newG = im.addInvGroup(invGrp);
				
				logger.debug("new group created: " + newG);
				
				return Response.ok(newG).build();

			}

		}catch(ValidationException ve) {
			status = Status.BAD_REQUEST;			
			msg = new Message("Validation", ve.getMessage());
		}catch(Exception ex) {
			logger.error("error in adding group user" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}


		return Response.status(status).entity(msg).build();
	}
	@Path("addItem")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response addItem() {
		logger.debug("addItem");

				Message msg = null;
				Status status = Status.FORBIDDEN;
		//		try {
		//		//check if the user has access to edit the users..
		//		if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite())) {
		//			
		//		}
		//		
		//	}catch(Exception ex) {
		//		logger.error("error in adding user" , ex);
		//		status = Status.INTERNAL_SERVER_ERROR;
		//		msg = new Message("Server error", ex.getMessage());
		//	}


		return Response.status(status).entity(msg).build();
	}
	@Path("updateItem")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response updateItem() {
		logger.debug("updateItem");

		Message msg = null;
		Status status = Status.FORBIDDEN;
		return Response.status(status).entity(msg).build();
	}
	/**
	 * Get all items for a group
	 * @return
	 */
	@Path("groupItems/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	@TokenValidation
	public Response getAllItemsForGroup(@PathParam("id") Integer groupId) {
		logger.debug("getAllItemsForGroup: " + groupId );//+  "  Length: " + (groupId != null ? groupId.length(): "null" ) );
		
		Message msg = null;
		Status status = Status.FORBIDDEN;
		
		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username
			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite())) {
				logger.debug("Has access.. fetching groups");
				
				//check if ID is passed
				if (groupId != null) { // && groupId.trim().length() >0) {
					InventoryManager im  = new InventoryManager();
					
//					int invGrpId = Integer.parseInt(groupId);
					
					List<Inventory> invList = im.loadAllInventoryByUserAndGroup(userReq.getId(), groupId);
					
					
					return Response.ok(invList).build();
				}				
				
				status = Status.BAD_REQUEST;
				msg = new Message("validation", "User group required");
			}

		
		}catch(Exception ex) {
			logger.error("error in adding group user" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}


		return Response.status(status).entity(msg).build();

	}
}
