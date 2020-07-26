package net.web.service.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.web.db.entity.User;
import net.web.manager.UserManager;
import net.web.service.model.Message;

@Provider
@TokenValidation
public class TokenListener implements ContainerRequestFilter{

	private static final Logger logger = LogManager.getLogger(TokenListener.class);

	private static final String REALM = "example"; //TODO
	private static final String AUTHENTICATION_SCHEME = "Bearer";
	
	@Context
	private transient HttpServletRequest servletRequest;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		logger.debug("Entering token listener");

	
		String authorizationHeader =
				requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		
		logger.debug("authorizationHeader: " + authorizationHeader);

		if (authorizationHeader != null &&
				authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase())) {
			
			String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
			
			
			User validUser = new UserManager().validateToken(token);
			
			logger.debug("Valid: " + validUser + " Token: " + token);

			//if token valid, continue
			if (validUser != null) {
				servletRequest.setAttribute("userRequest", validUser );
				return;
			}
		}
		
		
		abortWithUnauthorized(requestContext);

	}

	private void abortWithUnauthorized(ContainerRequestContext requestContext) {

		// Abort the filter chain with a 401 status code response
		// The WWW-Authenticate header is sent along with the response
		
		Message err = new Message("Token", "Invalid user token..");
		
		requestContext.abortWith(
				Response.status(Response.Status.UNAUTHORIZED)
				.header(HttpHeaders.WWW_AUTHENTICATE, 
						AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
				.entity(err)
				.build());
	}

}
