package net.web.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.web.db.entity.User;
import net.web.manager.UserManager;
import net.web.service.model.AuthUser;
import net.web.service.model.Message;

@Path("auth")
public class AuthService {
	
	private static final Logger logger = LogManager.getLogger(AuthService.class);

	@Path("login")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(AuthUser auth){

		logger.debug("Display user auth: " + auth);

		UserManager um = new UserManager();

		Message err= null;

		User user = null;

		try {  //123456
			

			user = um.loginUser(auth);

			if (user != null) {
				user.setUserWeb(null);			
				
				return Response.ok(user).build();
			}

			err = new Message("Login", "Invalid user login");
			
		}catch(IllegalAccessException i) {
			String message = i.getMessage();
			logger.info("Error with user. " + auth.getUserName() + ". Message: " + message);
			
			if ("Number of tries exceeded. locking account".equals(message)) {
				err = new Message("Locked", "Number of tries exceeded. locking account");
			}
			
		}
		catch (Exception ex) {
			logger.error("error in auth", ex);		
		}
		
		return Response.status(Status.UNAUTHORIZED).entity(err).build();//ok(obj).build();
	}
}
