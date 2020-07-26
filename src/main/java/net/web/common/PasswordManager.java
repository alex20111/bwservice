package net.web.common;

import com.lambdaworks.crypto.SCryptUtil;

/**
 * Central place to check and hash the user password.. 
 * 
 * @author ADMIN
 *
 */
public class PasswordManager {

	
	public static String hashPassword(String password) {
		return SCryptUtil.scrypt(password, 128, 8, 16);
		
	}
	
	public static boolean validate(String pass, String hashedPass) {
		return SCryptUtil.check(pass, hashedPass);
	}
}
