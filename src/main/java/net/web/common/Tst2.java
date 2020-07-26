package net.web.common;

import com.lambdaworks.crypto.SCryptUtil;

public class Tst2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	String password = "12345";
	String hashed = "$s0$70810$Cd5H+ZOFrkXzENV2LafZpg==$lM53+wQs+xDYiEFC/UqB3eFUfHGMM2/Fmlf4oRCMA58=";

	System.out.println(SCryptUtil.check(password, hashed));
	
	String hashed1 = PasswordManager.hashPassword(password);//, 128, 8, 16);
	
	System.out.println("Hashed1: " + hashed1);
	System.out.println(PasswordManager.validate(password, hashed1));

	}

}
