package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.*;

@Path("/update")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")

public class UpdateResource {
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public UpdateResource() {
	}

	@POST
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doUpdate(UpdateData data) {
		LOG.fine("Attempt to update user: " + data.username);

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);
			Key infoKey = datastore.newKeyFactory().addAncestors(PathElement.of("User", data.username))
					.setKind("UserInfo").newKey(data.username);
			Key loginKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
					.setKind("LoginInfo").newKey(data.username);
			Entity login = txn.get(loginKey);
			if (user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User " + data.username + " does not exist").build();

			}
			if (login == null) {

				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("You are currently not logged in").build();
			}
			if (!data.verifyToken(login.getLong("token_expiration"))) {
				txn.delete(loginKey);
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User login token is invalid").build();

			
			}
				Entity addInfo = txn.get(infoKey);
				if (!data.isValid()) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("The data inserted is not on the correct format").build();
				}
				addInfo = Entity.newBuilder(infoKey).set("user_profile", data.pType)
						.set("user_telefone_fixo", data.telefoneFixo)
						.set("user_telefone_movel", data.telefoneMovel)
						.set("user_address", data.morada)
						.set("user_comp_address", data.moradaComp)
						.set("user_localidade", data.localidade)
						.build();
				txn.update(user);
				txn.put(addInfo);
				LOG.info("User info updated " + data.username);
				txn.commit();
				return Response.ok("Profile was updated").build();
			

		} finally {
			if (txn.isActive())
				txn.rollback();
		}

	}

	@POST
	@Path("/role")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doUpdateRole(UpdateData user) {
		LOG.fine("Attempt to update user's role: " + user.username);

		Transaction txn = datastore.newTransaction();
		try {
			Key masterKey = datastore.newKeyFactory().setKind("User").newKey(user.username);
			Entity muser = txn.get(masterKey);
			Key targetKey = datastore.newKeyFactory().setKind("User").newKey(user.target);
			Entity tuser = txn.get(targetKey);
			Key loginKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", user.username))
					.setKind("LoginInfo").newKey(user.username);
			Entity login = txn.get(loginKey);
			if (muser == null || tuser == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();

			}
			if (user.param == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("No role was provided").build();

			}
			if (login == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("You are currently not logged in").build();
			}

			if (!user.verifyToken(login.getLong("token_expiration"))) {
				txn.delete(loginKey);
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User login token is invalid").build();

			}
			if (muser.getString("user_role").equals(Roles.GA.toString())) {
				if (tuser.getString("user_role").equals(Roles.USER.toString())) {
					if (user.param.equals(Roles.GBO.toString())) {
						Entity temp = Entity.newBuilder(tuser).set("user_role", user.param.toString()).build();
						txn.update(temp);
						LOG.info("User updated " + user.target);
						txn.commit();
						return Response.ok("User role updated").build();
					} else {
						txn.rollback();
						return Response.status(Status.BAD_REQUEST).entity("Can't update to that role").build();

					}

				} else {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Can't update that user").build();

				}

			} else if (muser.getString("user_role").equals(Roles.SU.toString())) {
				if (tuser.getString("user_role").equals(Roles.USER.toString())) {
					if (user.param.equals(Roles.GBO.toString()) || user.param.equals(Roles.GA.toString())) {
						Entity temp = Entity.newBuilder(tuser).set("user_role", user.param.toString()).build();
						txn.update(temp);
						LOG.info("User updated " + user.target);
						txn.commit();
						return Response.ok("User role updated").build();
					} else {
						txn.rollback();
						return Response.status(Status.BAD_REQUEST).entity("Can't update to that role").build();

					}

				} else {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Can't update that user").build();

				}

			} else {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("You don't have permissions to update that user")
						.build();

			}

		} finally {

			if (txn.isActive())
				txn.rollback();
		}
	}

	@POST
	@Path("/state")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doUpdateState(UpdateData user) {
		LOG.fine("Attempt to update user's state: " + user.username);

		Transaction txn = datastore.newTransaction();
		try {
			Key masterKey = datastore.newKeyFactory().setKind("User").newKey(user.username);
			Entity muser = txn.get(masterKey);
			Key targetKey = datastore.newKeyFactory().setKind("User").newKey(user.target);
			Entity tuser = txn.get(targetKey);
			Key loginKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", user.username))
					.setKind("LoginInfo").newKey(user.username);
			Entity login = txn.get(loginKey);

			if (muser == null || tuser == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Users can't update states").build();

			}
			if (login == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("You are currently not logged in").build();

			}
			if (!user.verifyToken(login.getLong("token_expiration"))) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User login token is invalid").build();

			}

			if (muser.getString("user_role").equals(Roles.USER.toString())) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();

			}
			if (user.isSuperior(muser, tuser)) {
				if (tuser.getString("user_state").equals(State.ENABLED.toString())) {
					Entity temp = Entity.newBuilder(tuser).set("user_state", State.DISABLED.toString()).build();
					txn.update(temp);

				} else {
					Entity temp = Entity.newBuilder(tuser).set("user_state", State.ENABLED.toString()).build();
					txn.update(temp);

				}
				LOG.info("User state updated " + user.target);
				txn.commit();
				return Response.ok("User state updated").build();
			}
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("You don't have permissions to update that user").build();

		} finally {

			if (txn.isActive())
				txn.rollback();
		}
	}

	@POST
	@Path("/password")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doUpdatePassword(RegisterData data) {
		LOG.fine("Attempt to update user's password: " + data.username);

		Transaction txn = datastore.newTransaction();
		try {
			Key masterKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity muser = txn.get(masterKey);
			
			if (muser == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Users can't update states").build();

			}

			if (!muser.getString("user_role").equals(Roles.USER.toString())) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Only role User can change password").build();

			}
			String hpwd = (String) muser.getString("user_pwd");

			if (!data.verifyNewPassword(hpwd)) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Can't change to that password").build();

			}

			Entity temp = Entity.newBuilder(muser).set("user_pwd", DigestUtils.sha512Hex(data.passwordNew)).build();
			txn.update(temp);
			txn.commit();
			LOG.info("User password updated " + data.username);
			return Response.ok("Password updated").build();

		} finally {

			if (txn.isActive())
				txn.rollback();
		}
	}
}
