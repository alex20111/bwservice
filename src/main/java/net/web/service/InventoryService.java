package net.web.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.web.common.Constants;
import net.web.db.entity.InventoryGroup;
import net.web.db.entity.User;
import net.web.enums.AccessLevel;
import net.web.manager.InventoryManager;
import net.web.manager.UserManager;
import net.web.service.filter.TokenValidation;
import net.web.service.model.Message;


public class InventoryService {

	private static final Logger logger = LogManager.getLogger(InventoryService.class);
	
	@Context
	private transient HttpServletRequest servletRequest;
	
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
	@Path("sideMenuItem")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response getAllSideMenuItemsForUser() {
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
}
