package net.web.manager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.web.common.Constants;
import net.web.common.PasswordManager;
import net.web.db.entity.User;
import net.web.db.entity.UserWeb;
import net.web.db.sql.UserSql;
import net.web.enums.Website;
import net.web.service.model.AuthUser;

public class UserManager {

	private static final Logger logger = LogManager.getLogger(UserManager.class);

	private UserSql sql;

	public UserManager() {
		sql = new UserSql();
	}

	public int addUser(User user) throws ClassNotFoundException, SQLException, IllegalAccessException {
		logger.debug("Adding user: " + user);
		//1st verify if user exist
		//then add user

		User findUser = sql.findByUserName(user.getUserName(), true);

		if (findUser == null) {
			//hash password before adding
			user.setPassword(PasswordManager.hashPassword(user.getPassword()));

			return  sql.add(user);		
		}else {
			throw new IllegalAccessException("User Name exist");
		}		
	}
	public void update(User user) throws ClassNotFoundException, SQLException {

		if (user != null) {
			sql.update(user);

			if (user.getUserWeb() != null && !user.getUserWeb().isEmpty()) {
				//update 
				List<UserWeb> toAdd = new ArrayList<UserWeb>();
				List<UserWeb> toDel = new ArrayList<UserWeb>();
				List<UserWeb> toUpdate = new ArrayList<UserWeb>();

				//1st fetch all for the user
				List<UserWeb> uwDbList = sql.fetchAccessForUser(user.getId());

				if (uwDbList.isEmpty()) {
					toAdd.addAll(user.getUserWeb());
				}else {

					for(UserWeb uw: user.getUserWeb()) {
						if (uw.getId() >= 0) {
							toUpdate.add(uw);
						}else if (uw.getId() == -1) {
							//add user to the class
							uw.setUserId(user.getId());
							toAdd.add(uw);
						}
					}
					//get the deleted ones
					for (UserWeb dbUw : uwDbList) {
						boolean found = false;
						for(UserWeb uw: user.getUserWeb()) {
							if(dbUw.getId() == uw.getId()) {
								found = true;
								break;
							}
						}

						if (!found) {
							toDel.add(dbUw);
						}
					}
				}


				if (!toAdd.isEmpty()) {
					sql.addUserWeb(toAdd);
					System.out.println("To Add: " + toAdd);
				}
				if (!toUpdate.isEmpty()) {
					sql.updateUserWeb(toUpdate);
					System.out.println("To update: " + toUpdate);
				}
				if (!toDel.isEmpty()) {
					sql.deleteUserWeb(toDel);
					System.out.println("To delete: " + toDel);
				}
			}

		}
	}
	public User loginUser(AuthUser auth) throws ClassNotFoundException, SQLException, IllegalAccessException {

		logger.debug("Logging user: " + auth.getUserName() + " pass: " + auth.getPassword());

		User user =  sql.findByUserName(auth.getUserName(), true);

		if (user != null && user.getNbOfTries() < 3) {
			logger.debug("user found: " + user);			 		 

			System.out.println("User found ---> " + user );
			//check 1st if the user has access to the website.
			Website website = Website.valueOf(auth.getWebsiteToAccess());
			if (user.hasWebsiteAccess(website)) {

				if (PasswordManager.validate(auth.getPassword(), user.getPassword()) ) {

					if (user.getNbOfTries() > 0) {
						user.setNbOfTries(0);
					}
					user.setLastLogin(new Date()); //no need to update access here

					long expire = new Date().getTime() + (1000 *60 *60 *12); //(1000 (1sec ) * 60 sec * 60 min * hours 
					//					long expire = new Date().getTime() + 10000; //(10 se for testing)
					String jwt = Jwts.builder()
							.setSubject("Token Validation") //userName
							.setExpiration(new Date(expire) ) //6 hours then force re-login
//							.setExpiration(new Date(new Date().getTime() +10000) ) //6 hours then force re-login
							.claim("userName", user.getUserName()) //first name
							.claim("website", auth.getWebsiteToAccess()) //last name
							.signWith(Constants.key)
							.compact();
					user.setAuthToken(jwt);
					sql.update(user);

					user.setPassword(null);
					user.setLastLogin(null);
					user.setNbOfTries(0);

					//get the access to the website
					List<UserWeb> uwl = user.getUserWeb().stream().filter(uacc -> uacc.getWebsiteAccess() == website).collect(Collectors.toList());

					user.setUserWeb(null);
					//set current access level to tell the UI.
					user.setAccess(uwl.get(0).getAccessLevel().name());				

					return user;
				}else {
					user.setNbOfTries(user.getNbOfTries() +1 );
					sql.update(user); //do not need to update web access
					throw new IllegalAccessException("Password mistmatch.. Wrong password");
				}

			}else {
				throw new IllegalAccessException("User does not have access to website");
			}
		}else if (user != null && user.getNbOfTries() > 2) {
			throw new IllegalAccessException("Number of tries exceeded. locking account");
		}

		logger.debug("User not found");

		return null;

	}
	/** 
	 * Validate a user when the user has already supposed to be logged in before. That is why we are not throwing errors and only
	 * returning true of false..
	 * 
	 * 
	 * @param auth - Authentication information
	 * @return true if account is valid and false if not.. 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public User validateToken(String token) {
		logger.debug("validateToken");

		if (token.trim().length() > 0) {

			try {
				Jws<Claims> jwsToken = Jwts.parserBuilder().setSigningKey(Constants.key).build().parseClaimsJws(token);

				String userName = (String)jwsToken.getBody().get("userName");
				String websiteAccess = (String)jwsToken.getBody().get("website");
				Website website = Website.valueOf(websiteAccess);

				User user = sql.findUserForValidation(userName);

				if (user != null && user.hasWebsiteAccess(website) ) {
					user.setCurrentWebsite(website);
					return user;
				}	
			}catch(Exception ex) {
				logger.error("Error",  ex);
				return null;
			}
		}
		return null;
	}

	public User findByUserName(String userName) throws ClassNotFoundException, SQLException {
		return sql.findByUserName(userName, true);
	}

	public void verifyIfTableExist() throws ClassNotFoundException, SQLException, IOException {
		new UserSql().CreateUserTable();
	}

	public List<User> loadAllUsers(Website website, boolean withAccess, String... fields) throws ClassNotFoundException, SQLException{
		List<User> userList = new ArrayList<User>();
		if (website == Website.SERVICE_WEBSITE) {
			//load all users
			userList = sql.loadAllUsers(withAccess, fields);
		}else {
			//load users for specific website //TODO
		}


		return userList;
	}

	public User loadUserById(int id, boolean withAccess) throws ClassNotFoundException, SQLException {
		return sql.loadUserById(id, withAccess);
	}

	public void deleteUser(int id) throws ClassNotFoundException, SQLException {
		logger.debug("deleting user: " + id);

		//1st load user
		User usr = sql.loadUserById(id, false);

		if (usr != null) {

			sql.deleteUser(id);
		}
		else {
			throw new IllegalAccessError("No user with that ID exist. ID: " + id);
		}
	}
	
	public void changePassword(int userId, String password) throws ClassNotFoundException, SQLException {
		
		password = PasswordManager.hashPassword(password);
		
		sql.updatePassword(userId, password);
	}


	public static void main(String args[]) throws ClassNotFoundException, SQLException, IllegalAccessException, IOException {
		UserManager m = new UserManager();
		Constants.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

		//		m.verifyIfTableExist();

		
		m.verifyIfTableExist();

//		String userToUse = "test3";
//		String password =  SCryptUtil.scrypt("12345", 128, 8, 16);	 //Gotten from DB
//
//		System.out.println("Testing started of user");
//
//		//		System.out.println("Find by username");
//		//		User user1 = m.findByUserName(userToUse);
//		//		System.out.println("Result of find: " + user1);
//
//		//ADDING USER
//		try {
//			System.out.println("Adding user");
//			User user2 = new User();
//			user2.setEmail("bob@yahoo1.com");
//			user2.setFirstName("bob4");
//			user2.setLastName("l'epponge ---kiloki");
//			user2.setPassword(password);
//			user2.setLastLogin(new Date());
//			user2.setUserName(userToUse);
//
//			List<UserWeb> userWebList = new ArrayList<UserWeb>();
//			UserWeb uw = new UserWeb();
//			uw.setAccessLevel(AccessLevel.ADMIN);
//			uw.setWebsiteAccess(Website.SERVICE_WEBSITE);
//
//			userWebList.add(uw);
//			user2.setUserWeb(userWebList);
//
//			m.addUser(user2);
//		}catch(IllegalAccessException ix) {
//			System.out.println(" !! ---- User Name already exist. --- !!!S ");
//		}
//		//display added user
//		System.out.println("Display user addded");
//
//		User user1 = m.findByUserName(userToUse);
//		System.out.println("Result of find2: " + user1 + "  access: "+ user1.getUserWeb());
		//
		//		//Test Login
		//		System.out.println("Test valid login on website that the user has access to");
		//		AuthUser au = new AuthUser();
		//		au.setUserName(userToUse);
		//		au.setPassword("123456");
		//		au.setWebsiteToAccess(Website.SERVICE_WEBSITE.name());
		//		User loggedUser = m.loginUser(au);
		//		System.out.println("Logged in user: " + loggedUser);
		//
		//		//Test Login2
		//		try {
		//			System.out.println("Test invalid login on website that the user has access to");
		//			au = new AuthUser();
		//			au.setUserName(userToUse);
		//			au.setPassword(password);
		//			au.setWebsiteToAccess(Website.ISABELLE.name());
		//			User loggedUser2 = m.loginUser(au);
		//			System.out.println("Logged in user: " + loggedUser2 );
		//		}catch(Exception ex) {
		//			ex.printStackTrace();
		//		}
		//
		//		System.out.println(" !!!  Validate token !!! ");
		//		User valid = m.validateToken(loggedUser.getAuthToken());
		//
		//		System.out.println("User valid token: " + valid);
		//		
		//		System.out.println(" !!! Update user !!!");
		//		User userToUpdate = m.findByUserName(userToUse);
		//		List<UserWeb> luw = userToUpdate.getUserWeb();
		//		//add
		//		UserWeb u = new UserWeb();
		//		u.setAccessLevel(AccessLevel.REGULAR);
		//		u.setWebsiteAccess(Website.SERVICE_WEBSITE);
		//		u.setUserId(userToUpdate.getId());
		//		luw.add(u);
		//		//upd
		//		UserWeb uu = luw.get(1);
		//		uu.setAccessLevel(AccessLevel.VIEW);
		//		
		//		//todel
		//		luw.remove(0);
		//		
		//		m.update(userToUpdate);


		System.out.println(" !!!!!!!!!!1  END !!!!!!!!!!!!!");




	}
}
