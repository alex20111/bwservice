package net.web.service;

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

import home.common.data.Temperature;
import net.web.common.Constants;
import net.web.db.entity.User;
import net.web.enums.AccessLevel;
import net.web.enums.Website;
import net.web.service.filter.TokenValidation;
import net.web.service.model.Message;

@Path("temperature")
public class TemperatureService {

	
	private static final Logger logger = LogManager.getLogger(TemperatureService.class);
	private static  Temperature tempStorage = new Temperature();
	
	@Context
	private transient HttpServletRequest servletRequest;

	@Path("update")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response updateTemperature(Temperature temp){
		
		logger.debug("Updating temp: " + temp);
		
		Message msg = null;
		Status status = Status.FORBIDDEN;
		
		User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username

		//check if the user has access to edit the users..
		if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite()) && Website.HEADLESS == userReq.getCurrentWebsite()) {

			tempStorage = temp;
			
			logger.debug("temp in storage: " + tempStorage);
			
			msg = new Message("Success", "temperature updated");
			return Response.ok().entity(msg).build();
			
		}else {
			msg = new Message("No Access", "User does not have permission");
		}
		
		return Response.status(status).entity(msg).build();
	}
	@Path("temp")
	@GET
	@Produces(MediaType.APPLICATION_JSON)	
	public Response getTemp() {
		
		logger.debug("Getting temperature: " + tempStorage);
		
		
		return Response.ok(tempStorage).build();
	}

}
