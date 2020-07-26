package net.web.db.entity;

import com.lambdaworks.crypto.SCryptUtil;

public abstract class TestScrpy {

	public static void main(String[] args) {
        String originalPassword = "password";
        String generatedSecuredPasswordHash = SCryptUtil.scrypt(originalPassword, 256, 8, 16);
        System.out.println(generatedSecuredPasswordHash);
         
        boolean matched = SCryptUtil.check("password", generatedSecuredPasswordHash);
        System.out.println(matched);
         
        matched = SCryptUtil.check("passwordno", generatedSecuredPasswordHash);
        System.out.println(matched);
	}

}
