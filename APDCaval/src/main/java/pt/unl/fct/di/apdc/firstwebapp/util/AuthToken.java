package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.Date;
import java.util.UUID;

public class AuthToken {
	public static final long EXPIRATION_TIME = 1000 * 60 * 60;
	public String username;
	public String tokenID;
	public long creationData;
	public long expirationData;
	public String role;
	public Date date;


	public AuthToken(String username,String role) {
		this.username = username;
		this.tokenID = UUID.randomUUID().toString();
		this.role=role;
		this.creationData = System.currentTimeMillis();
		this.expirationData = this.creationData + AuthToken.EXPIRATION_TIME;
	}
}