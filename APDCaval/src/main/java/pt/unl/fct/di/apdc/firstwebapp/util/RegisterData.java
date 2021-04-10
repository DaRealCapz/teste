package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;

public class RegisterData {


	public String username;
	public String password;
	public String email;
	public String passwordConf1;
	public String passwordConf2;
	public String passwordNew;



	public RegisterData() {

	}
	public RegisterData(String username,String password,String passwordConf1,String email) {
		this.password=password;
		this.username=username;
		this.email=email;
		this.passwordConf1=passwordConf1;
	
	}
	public RegisterData(String username,String passwordNew, String passwordConf1) {
		this.username = username;
		this.passwordNew = passwordNew;
		this.passwordConf1 = passwordConf1;
	}

	
	
	public boolean validRegistration() {
		if(username.equals(""))
			return false;
		if(!password.equals(passwordConf1))
			return false;
		Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
		Matcher mat = pattern.matcher(email);
		if(!mat.matches())
			return false;

		return true;
	}
	public boolean verifyNewPassword(String hpwd) {
		if (!hpwd.equals(DigestUtils.sha512Hex(passwordNew))) {
			if (passwordNew.equals(passwordConf1)) {
				return true;
			}else {
				return false;
			}
		} else
			return false;
	}
	
}
