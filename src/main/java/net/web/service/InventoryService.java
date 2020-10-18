package net.web.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;
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
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.imgscalr.Scalr;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.web.common.Constants;
import net.web.db.entity.Inventory;
import net.web.db.entity.InventoryGroup;
import net.web.db.entity.User;
import net.web.enums.AccessLevel;
import net.web.exception.ValidationException;
import net.web.manager.InventoryManager;
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
				InventoryGroup ig = new InventoryGroup();
				ig.setGroupName("All items");
				InventoryManager im = new InventoryManager();

				List<Inventory> invList = im.loadAllInventoryForUser(userReq.getId(), true);

				ig.setInvItems(invList);
				return Response.ok().entity(ig).build();
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
	@Path("updateGroupName")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response updateGroupName(InventoryGroup invGrp) {
		logger.debug("updateGroupName: " + invGrp);

		Message msg = null;
		Status status = Status.FORBIDDEN;
		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username
			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite())) {
				logger.debug("Has access.. updating group");

				InventoryManager im = new InventoryManager();
				invGrp.setOwnerId(userReq.getId());
				im.updateInvGroup(invGrp);

				logger.debug("new group created: " + invGrp);

				return Response.ok().entity(invGrp).build();

			}

		}catch(Exception ex) {
			logger.error("error in adding group user" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}


		return Response.status(status).entity(msg).build();
	}
	@Path("updateItem")
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Produces(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response updateItem(  @FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition fileMetaData,
			@FormDataParam("formField") String inv) throws Exception
	{

		logger.debug("updateItem: " + inv);

		Message msg = null;
		Status status = Status.FORBIDDEN;

		if (inv == null || inv.trim().length() == 0) {
			status = Status.BAD_REQUEST;			
			msg = new Message("Validation", "Missing form");
			return Response.status(status).entity(msg).build();	
		}

		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username
			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite())) {
				logger.debug("Has access.. updating Inventory item");


				ObjectMapper objectMapper = new ObjectMapper();

				Inventory inventory = objectMapper.readValue(inv, Inventory.class);
				inventory.setOwnerId(userReq.getId());
				
				if (fileInputStream != null && fileMetaData != null) {

					inventory.setThumbBase64(imageToBase64(fileInputStream, fileMetaData));
				}
				
				InventoryManager im = new InventoryManager();
				im.update(inventory);
				
				msg = new Message("Success", "Item updated");
				
				return Response.ok().entity(msg).build();
			}

		}catch(Throwable ex) {
			logger.error("error in adding group user" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}

		return Response.status(status).entity(msg).build();
	}
	/**
	 * Get all items for a group
	 * 
	 * @PathParam("id")  -- accept a parameter on the url as ID.
	 * @return
	 */
	@Path("groupItems/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	@TokenValidation
	public Response getAllItemsForGroup(@PathParam("id") Integer groupId) {
		logger.debug("getAllItemsForGroup: " + groupId );

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

					List<Inventory> invList = im.loadAllInventoryByUserAndGroup(userReq.getId(), groupId);

					InventoryGroup grp = im.loadInvetoryGroupById(groupId);
					grp.setInvItems(invList);

					return Response.ok(grp).build();
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
	@Path("addItem")
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Produces(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response addInvItem(  @FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition fileMetaData,
			@FormDataParam("formField") String inv) throws Exception
	{
		logger.debug("addInvItem: File: " + fileMetaData + "  formField: " + inv);	

		Message msg = null;
		Status status = Status.FORBIDDEN;

		if (inv == null || inv.trim().length() == 0) {
			status = Status.BAD_REQUEST;			
			msg = new Message("Validation", "Missing form");
			return Response.status(status).entity(msg).build();	
		}

		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username
			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite())) {
				logger.debug("Has access.. adding Inventory item");


				ObjectMapper objectMapper = new ObjectMapper();

				Inventory inventory = objectMapper.readValue(inv, Inventory.class);
				inventory.setOwnerId(userReq.getId());

				if (fileInputStream != null && fileMetaData != null) {

					//					String UPLOAD_PATH = "c:\\temp\\"; //TODO TEMP DIR
					//					String fileName = fileMetaData.getFileName() + UUID.randomUUID();				
					//
					//					File tumb = new File(UPLOAD_PATH + fileName);
					//					int read = 0;
					//					byte[] bytes = new byte[1024];
					//
					//					OutputStream out = new FileOutputStream(new File(UPLOAD_PATH + fileName));
					//					while ((read = fileInputStream.read(bytes)) != -1) 
					//					{
					//						out.write(bytes, 0, read);
					//					}
					//					out.flush();
					//					out.close();

					//then convert it to bitap
					//encode it and save


					inventory.setThumbBase64(imageToBase64(fileInputStream, fileMetaData));
					//
					//then delete file
					//					tumb.delete();
				}

				InventoryManager im = new InventoryManager();

				Inventory newInv = im.add(inventory);
				return Response.ok().entity(newInv).build();
			}

		}catch(Exception ex) {
			logger.error("error in adding group user" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}

		return Response.status(status).entity(msg).build();

	}
	@Path("deleteGroup")
	@POST
	@Produces(MediaType.APPLICATION_JSON )
	@Consumes(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response deteleGroup(String groupId) {
		logger.debug("deteleGroup: " + groupId);

		Message msg = null;
		Status status = Status.FORBIDDEN;
		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username
			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite())) {
				logger.debug("Has access.. deleting group");

				InventoryManager im = new InventoryManager();

				im.deleteGroup( Integer.parseInt(groupId),userReq.getId());

				msg = new Message("success", "Group deleted");

				return Response.ok().entity(msg).build();

			}

		}catch(Exception ex) {
			logger.error("error in adding group user" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}


		return Response.status(status).entity(msg).build();
	}
	@Path("deleteItem")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response deteleItem(String itemId) {
		logger.debug("deteleItem: " + itemId);

		Message msg = null;
		Status status = Status.FORBIDDEN;
		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username
			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite())) {
				logger.debug("Has access.. Deleting group");

				InventoryManager im = new InventoryManager();
				im.deleteItem(Integer.parseInt(itemId));

				msg = new Message("success", "Item deleted");

				return Response.ok(msg).build();

			}
		}catch(Exception ex) {
			logger.error("error in adding group user" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}


		return Response.status(status).entity(msg).build();
	}
	@Path("search/{searchQuery}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response search(@PathParam("searchQuery") String text) {
		logger.debug("Search text: " + text);		
		
		Message msg = null;
		Status status = Status.FORBIDDEN;
		try {
			User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username
			//check if the user has access to edit the users..
			if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite())) {
				logger.debug("Has access.. Search inventory");

				InventoryManager im = new InventoryManager();
				List<Inventory> invList = im.search(text);
				
				InventoryGroup ig = new InventoryGroup();
				ig.setInvItems(invList);
				ig.setGroupName("Search Result");
				ig.setNumberOfItems(invList.size());

				return Response.ok().entity(ig).build();

			}
		}catch(Exception ex) {
			logger.error("error in inventory search" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message("Server error", ex.getMessage());
		}
		return Response.status(status).entity(msg).build();

	}
	
	
	private String imageToBase64(InputStream fileInputStream, FormDataContentDisposition fileMetaData) throws IOException {
		String UPLOAD_PATH = "c:\\temp\\"; //TODO TEMP DIR
		String fileName = fileMetaData.getFileName() + UUID.randomUUID();				

		File tumb = new File(UPLOAD_PATH + fileName);
		int read = 0;
		byte[] bytes = new byte[1024];

		OutputStream out = new FileOutputStream(new File(UPLOAD_PATH + fileName));
		while ((read = fileInputStream.read(bytes)) != -1) 
		{
			out.write(bytes, 0, read);
		}
		out.flush();
		out.close();

		//then convert it to bitap
		//encode it and save
		BufferedImage bi = Scalr.resize(ImageIO.read(tumb), 100);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(bi, "jpg", output);

		//then delete file
		tumb.delete();

		return DatatypeConverter.printBase64Binary(output.toByteArray());
	}
	private String imgToBase64( BufferedImage bi ) throws IOException{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(bi, "jpg", output);
		return DatatypeConverter.printBase64Binary(output.toByteArray());
	}
}
