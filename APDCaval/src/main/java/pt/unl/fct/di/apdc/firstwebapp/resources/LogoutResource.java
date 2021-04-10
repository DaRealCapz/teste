package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.*;

@Path("/logout")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

	private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LogoutResource() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLogout(LoginData data) {
		LOG.fine("Attempt to logout user: " + data.username);

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Key loginKey = datastore.newKeyFactory().addAncestors(PathElement.of("User", data.username))
					.setKind("LoginInfo").newKey(data.username);
			Entity user = txn.get(userKey);
			Entity login = txn.get(loginKey);
			if (user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("This user does not exist!").build();

			}
			if (login != null) {

					txn.delete(loginKey);
					txn.commit();
					return Response.ok("User " + data.username + " is now logged out!").build();
				
			}else {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("No login token detected").build();

			}
			
		} catch (Exception e) {
			txn.rollback();
			
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}

	

}
