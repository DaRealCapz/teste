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

@Path("/delete")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")


public class DeleteResource {
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	public DeleteResource() {}
	

@POST
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
public Response doDeletion(UpdateData data) {
	LOG.fine("Attempt to delete user: " + data.username);
	
	Transaction txn = datastore.newTransaction();
	try {
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = txn.get(userKey);
		Key targetKey = datastore.newKeyFactory().setKind("User").newKey(data.target);
		Entity target = txn.get(targetKey);
		Key loginKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
				.setKind("LoginInfo").newKey(data.username);
		Entity login = txn.get(loginKey);
		
		
		
		if(user==null||target==null) {
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
		}
		if(login==null) {
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("You are currently not logged in").build();

		}
		if(!data.verifyToken(login.getLong("token_expiration"))) {
			txn.delete(loginKey);
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User login token is invalid").build();

		}
		
		if(user.getString("user_role").equals(Roles.USER.toString() )&& data.username.equals(data.target)) {
			Key infoKey = datastore.newKeyFactory().setKind("UserInfo").newKey(data.username);
			Entity info = txn.get(infoKey);
			if(info==null)
				txn.delete(userKey,loginKey);
			else
				txn.delete(userKey,loginKey,infoKey);
			
			LOG.info("User deleted " + data.username);
			txn.commit();
			return Response.ok("User was deleted").build();
		}
		if(target.getString("user_role").equals(Roles.USER.toString()) &&((user.getString("user_role").equals(Roles.GA.toString())||user.getString("user_role").equals(Roles.GBO.toString())))) {
			Key infoKey = datastore.newKeyFactory().setKind("UserInfo").newKey(data.target);
			Entity info = txn.get(infoKey);
			Key loginKey2 = datastore.newKeyFactory().addAncestor(PathElement.of("User", data.target))
					.setKind("LoginInfo").newKey(data.target);
			if(info==null)
				txn.delete(targetKey,loginKey2);
			else
				txn.delete(targetKey,loginKey2,infoKey);
			LOG.info("User deleted " + data.username);
			txn.commit();
			return Response.ok("User was deleted").build();
		}else {
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User does not have clearence to do that").build();
		}
	}finally {
		if(txn.isActive())
			txn.rollback();
	}
	
}

	
}
