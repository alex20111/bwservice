package net.web.listener;


import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.web.common.Constants;
import net.web.db.sql.InventorySql;
import net.web.db.sql.TempSql;
import net.web.enums.OsType;
import net.web.manager.UserManager;



public class InitManager implements ServletContextListener    {

	private static final Logger logger = LogManager.getLogger(InitManager.class);
	

	/* Application Startup Event */
	public void contextInitialized(ServletContextEvent ce) 
	{
		logger.debug("Context called at init");
		System.out.println("In init .. Loaded");
		
		UserManager um = new UserManager();
		try {
			um.verifyIfTableExist(); 
////			User user2 = new User();
////			user2.setEmail("bobTest3@yahoo.com");
////			user2.setFirstName("bob - Tes");
////			user2.setLastName("l'epponge");
////			user2.setPassword("12345");
////			user2.setLastLogin(new Date());
////			user2.setUserName("test11");
////
////			List<UserWeb> userWebList = new ArrayList<UserWeb>();
////			UserWeb uw = new UserWeb();
////			uw.setAccessLevel(AccessLevel.ADMIN);
////			uw.setWebsiteAccess(Website.SERVICE_WEBSITE);
////
////			userWebList.add(uw);
////			user2.setUserWeb(userWebList);
////			um.addUser(user2);
//			
			
			InventorySql invSql = new InventorySql();
			invSql.createTables();
			
			TempSql tmpSql = new TempSql();
			tmpSql.createTable();
			
			
			//find OS type to set tmp directory
			String osType = System.getProperty("os.name");
			if (osType.contains("Windows")) {
				Constants.osType = OsType.WINDOWS;
			}else {
				Constants.osType = OsType.LINUX;
			}
			
			
		} catch (ClassNotFoundException | SQLException |  IOException e) {
			// TODO Auto-generated catch block
			logger.error("error in init",  e);
		}

		
		Constants.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		
		logger.debug("contextInitialized end");
	}


	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}

}
