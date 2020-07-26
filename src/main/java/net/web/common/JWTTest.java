package net.web.common;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

public class JWTTest {

	public static void main(String[] args) throws InvalidKeyException, UnsupportedEncodingException {

		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		
		System.out.println();
		long nda = new Date().getTime() + 10000;
		
		String jwt = Jwts.builder()
				  .setSubject("bobbie") //userName
				  .setExpiration(new Date(nda)) //5 hours then force re-login
				  .claim("name", "Robert Token Man") //first name
				  .claim("scope", "self groups/admins") //last name
				  .claim("idName", "33") //User id.
				  .signWith(key)
				  .compact();
		
		System.out.println("JWT: " + jwt);
		System.out.println("JWT: " + jwt.length());
		
		System.out.println(Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody().get("idName", String.class));//tSubject().equals("bobbie"));
		System.out.println();
	}

}
