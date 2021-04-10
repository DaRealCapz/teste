package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.cloud.datastore.Entity;

public class UpdateData {
	public String pType;
	public String telefoneFixo;
	public String telefoneMovel;
	public String morada;
	public String moradaComp;
	public String localidade;
	public String username;
	public String target;
	public String param;
	public String passwordNew;
	public String passwordConf;

	public UpdateData() {
	}

	public UpdateData(String username, String pType, String telefoneFixo, String telefoneMovel, String morada,
			String moradaComp, String localidade) {
		this.username = username;
		this.pType = pType;
		this.telefoneFixo = telefoneFixo;
		this.telefoneMovel = telefoneMovel;
		this.morada = morada;
		this.moradaComp = moradaComp;
		this.localidade = localidade;

	}

	public UpdateData(String username, String target, String param) {
		this.username = username;
		this.target = target;
		this.param = param;
	}

	
	public boolean isSuperior(Entity user, Entity target) {
		if (user.getString("user_role").equals(Roles.SU.toString()))
			return true;
		if (user.getString("user_role").equals(Roles.GA.toString())
				&& (target.getString("user_role").equals(Roles.GBO.toString())
						|| target.getString("user_role").equals(Roles.USER.toString())))
			return true;
		if (user.getString("user_role").equals(Roles.GBO.toString())
				&& target.getString("user_role").equals(Roles.USER.toString()))
			return true;

		return false;
	}

	public boolean verifyToken(long time) {

		long currentTime = System.currentTimeMillis();
		if (time > currentTime)
			return true;
		else
			return false;
	}

	public boolean isValid() {
		
		
		Pattern telefone = Pattern.compile("^((\\+351|00351|351)?) (2\\d{1}|(9(3|6|1)))\\d{7}$");
		Matcher mat = telefone.matcher(telefoneFixo);
		Matcher mat1 =telefone.matcher(telefoneMovel);
		if(!mat.matches()||!mat1.matches())
			return false;
		Pattern cp = Pattern.compile("^[0-9]{4}-[0-9]{3}$");
		Matcher mat2 = cp.matcher(localidade);
		if(!mat2.matches())
			return false;
		if(pType.equals("Publico")|| pType.equals("Privado"))
			return true;	
		 		
		 
		
		return true;
	}

	
}
