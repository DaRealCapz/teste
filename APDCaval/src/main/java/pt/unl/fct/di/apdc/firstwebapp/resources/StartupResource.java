package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.Roles;
import pt.unl.fct.di.apdc.firstwebapp.util.State;

public class StartupResource implements ServletContextListener {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public StartupResource() {
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey("admin");
			Key loginKey = datastore.newKeyFactory().addAncestors(PathElement.of("User", "admin"))
					.setKind("LoginInfo").newKey("admin");
			
			
					AuthToken token = new AuthToken("admin", Roles.SU.toString());

				Entity	login = Entity.newBuilder(loginKey)
							.set("user", token.username)
							.set("token_value", token.tokenID)
							.set("token_creation_date", token.creationData)
							.set("token_expiration",  Long.MAX_VALUE)
							.set("token_role", token.role).build();

				Entity user = Entity.newBuilder(userKey)
						.set("user_pwd", DigestUtils.sha512Hex("oneringtorulethemall"))
						.set("user_email", "admin@admin.pt")
						.set("user_role", Roles.SU.toString())
						.set("user_state", State.ENABLED.toString())
						.set("user_creation_time", Timestamp.now())
						.build();
					txn.put(login,user);
					
					txn.commit();
				

		}finally {
			if(txn.isActive())
				txn.rollback();
		}
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}

}
