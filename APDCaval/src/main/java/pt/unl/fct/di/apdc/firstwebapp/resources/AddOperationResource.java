package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.List;

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
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.gson.Gson;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.*;


@Path("/op8")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")

public class AddOperationResource {
	


	
		private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		private final Gson g = new Gson();

		public AddOperationResource() {}
		
		@POST
		@Path("/1")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
		public Response showRoleUsers(QueryData data) {
			
			Transaction txn = datastore.newTransaction();

			try {
				Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
				Entity user = datastore.get(userKey);
				Key loginKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
						.setKind("LoginInfo").newKey(data.username);
				Entity login = txn.get(loginKey);
				
				if(user==null) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User does't exist").build();

				}
				if(login==null) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("You are not logged in").build();

				}
				if (login.getLong("token_expiration")>System.currentTimeMillis()) {
					txn.delete(loginKey);
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User login token is invalid").build();

				}
				if(!user.getString("user_role").equals(Roles.GBO.toString())) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("You don't have permission").build();

				}
				
				Query<Entity> query = Query.newEntityQueryBuilder()
						.setKind("User")
						.setFilter(
								CompositeFilter.and(PropertyFilter.eq("user_role", data.param))).build();
				QueryResults<Entity> logs = datastore.run(query);
				
				List<String> users = new ArrayList();
				
				
				logs.forEachRemaining(curr->{
					users.add(curr.getKey().getName());
				});
				return Response.ok(g.toJson(users)).build();
				
			}finally {
				if(txn.isActive())
					txn.rollback();
			}
			
			
			
			
			
			
			
			
			
			
		}
		@POST
		@Path("/2")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
		public Response showAttUsers(QueryData data) {
			
			Transaction txn = datastore.newTransaction();

			try {
				Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
				Entity user = datastore.get(userKey);
				Key loginKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
						.setKind("LoginInfo").newKey(data.username);
				Entity login = txn.get(loginKey);
				Key infoKey = datastore.newKeyFactory().addAncestor(PathElement.of("User",data.param))
						.setKind("UserInfo").newKey(data.param);
				Entity info = txn.get(infoKey);
				
				if(user==null) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User does't exist").build();

				}

				if(info==null) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User didn't register any additional info").build();

				}
				if(login==null) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("You are not logged in").build();

				}
				if (login.getLong("token_expiration")>System.currentTimeMillis()) {
					txn.delete(loginKey);
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User login token is invalid").build();

				}
				if(!user.getString("user_role").equals(Roles.GA.toString())) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("You don't have permission").build();

				}
				
				return Response.ok(g.toJson(info)).build();
				
			}finally {
				if(txn.isActive())
					txn.rollback();
			}
			
			
			
			
			
			
			
			
			
			
		}
}
