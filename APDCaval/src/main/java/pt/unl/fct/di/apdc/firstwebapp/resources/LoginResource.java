package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.gson.Gson;
import com.google.protobuf.StringValue;

import pt.unl.fct.di.apdc.firstwebapp.util.*;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LoginResource() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLogin(LoginData data) {
		LOG.fine("Attempt to login user: " + data.username);

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
			if (login == null) {
				String hpwd = (String) user.getString("user_pwd");
				if (hpwd.equals(DigestUtils.sha512Hex(data.password))) {
					AuthToken token = new AuthToken(data.username, user.getString("user_role"));

					login = Entity.newBuilder(loginKey)
							.set("user", token.username)
							.set("token_value", token.tokenID)
							.set("token_creation_date", token.creationData)
							.set("token_expiration", token.expirationData)
							.set("token_role", token.role).build();

				
					txn.put(login);
					
					txn.commit();
					return Response.ok("User " + token.username + " is now logged in!").build();
				} else {// pass errada
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Wrong password or username").build();

				}
			}else {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Already logged in").build();

			}
			
		

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}

	

}
