package net.web.service;

import java.sql.SQLException;
import java.util.ArrayList;
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
import net.web.db.entity.User;
import net.web.enums.AccessLevel;
import net.web.manager.UserManager;
import net.web.service.filter.TokenValidation;
import net.web.service.model.Message;
@Path("user")
public class UserService {

	private static final Logger logger = LogManager.getLogger(UserService.class);


	@Context
	private transient HttpServletRequest servletRequest;

	@Path("allUsers")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response getAllUsers(){

		UserManager um = new UserManager();

		List<User> userList = new ArrayList<User>();

		User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username

		//check if the user has access to edit the users..
		if (userReq.hasPermission(AccessLevel.ADMIN, userReq.getCurrentWebsite())) {
			logger.debug("has permission to get all users");
			try {
				userList = um.loadAllUsers( userReq.getCurrentWebsite(), true, User.ID, User.USER_NAME, User.LAST_NAME, User.FIRST_NAME, User.LAST_LOGIN, User.EMAIL);//TODO return users depending on the website accessing it.

			} catch (ClassNotFoundException | SQLException e) {
				logger.error("error", e);
				return Response.status(Response.Status.FORBIDDEN).build();
			}

		}else {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		return Response.ok(userList).build();
	}

	@Path("update")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response updateUser(User user){

		Message msg = null;
		Status status = Status.FORBIDDEN;

		logger.debug("Update user: " + user);
		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username

			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.ADMIN, userReq.getCurrentWebsite())) {
				logger.debug("has permission to update the user");

				UserManager um = new UserManager();

				//1st get user to see if it exist
				User userFromDb = um.loadUserById(userReq.getId(), false);

				if (userFromDb != null) {
					//update certain fields.
					//email, first name, last name, user name, access type
					user.setLastLogin(userFromDb.getLastLogin());
					user.setNbOfTries(userFromDb.getNbOfTries());
					user.setPassword(userFromDb.getPassword());
					user.setAuthToken(userFromDb.getAuthToken());

					um.update(user);

					return Response.ok().entity(new Message("success", "user updated")).build();
				}else {
					status = Status.NOT_FOUND;
					msg = new Message("Not found", "entity not found");
					return Response.status(status).entity(msg).build();
				}
			}else {
				msg = new Message("No Access", "User does not have permission");
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}

		return Response.status(status).entity(msg).build();
	}
	@Path("add")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response addUser(User user){

		logger.debug("add user: " + user);

		Message msg = null;
		Status status = Status.FORBIDDEN;

		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username

			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.ADMIN, userReq.getCurrentWebsite())) {
				logger.debug("has permission to add the user");
				UserManager um = new UserManager();
				
				um.addUser(user);
				msg = new Message("success","user added");
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
	@Path("delete")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response deleteUser(int id){
		logger.debug("delete user: " + id);

		Message msg = null;
		Status status = Status.FORBIDDEN;

		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username

			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.ADMIN, userReq.getCurrentWebsite())) {
				logger.debug("has permission to delete the user");
				UserManager um = new UserManager();
				
				um.deleteUser(id);
				msg = new Message("success","user deleted");
				return Response.ok().entity(msg).build();
			}
			else {
				msg = new Message("No Access", "User does not have permission");
			}
		}catch(Exception ex) {
			logger.error("error in deleting user" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}

		return Response.status(status).entity(msg).build();
	}
	@Path("updPass")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	@TokenValidation
	public Response changePassword(String password){
		logger.debug("cahange password user: " + password);

		Message msg = null;
		Status status = Status.FORBIDDEN;

		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username

			//check if the user has access to edit the users..
			if (userReq.hasWebsiteAccess(userReq.getCurrentWebsite())) {
				logger.debug("can update password ");
				UserManager um = new UserManager();
				
				um.changePassword(userReq.getId(), password);
				
				msg = new Message("success","Password Changed");
				return Response.ok().entity(msg).build();
			}
			else {
				msg = new Message("No Access", "User does not have permission");
			}
		}catch(Exception ex) {
			logger.error("error in changing password" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}

		return Response.status(status).entity(msg).build();
	}
	
	@Path("getById")
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response getUserById(String id){

		Message msg = null;
		Status status = Status.FORBIDDEN;

		logger.debug("Get user by ID: " + id);
		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username

			int userId = Integer.parseInt(id);
			
			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.ADMIN, userReq.getCurrentWebsite()) || userReq.getId() == userId ) {
				logger.debug("has permission to get the user. User: " + userReq.getUserName());

				UserManager um = new UserManager();

				//1st get user to see if it exist
				User userFromDb = um.loadUserById(userId, true);

				if (userFromDb != null) {
					userFromDb.setNbOfTries(0);
					userFromDb.setAuthToken(null);
					userFromDb.setPassword(null);
					userFromDb.setLastLogin(null);
					return Response.ok().entity(userFromDb).build();
				}else {
					status = Status.NOT_FOUND;
					msg = new Message("Not found", "entity not found");
					return Response.status(status).entity(msg).build();
				}
			}else {
				msg = new Message("No Access", "User does not have permission");
			}
		}catch(Exception ex) {
			logger.error("error in getUserById" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}

		return Response.status(status).entity(msg).build();
	}
	
	//	 User [id=1, userName=test1, password=, firstName=bob, lastName=l'epponge, email=bob@yahoo.com, lastLogin=Mon Jun 29 14:48:56 EDT 2020, nbOfTries=0, token= 
	//	Access: [UserWeb [id=1, accessLevel=ADMIN, websiteAccess=SERVICE_WEBSITE, userId=1],
	//	UserWeb [id=-1, accessLevel=UNAUTHORIZED, websiteAccess=ISABELLE, userId=-1],
	//	UserWeb [id=-1, accessLevel=UNAUTHORIZED, websiteAccess=MATHIEU, userId=-1]]]

}
