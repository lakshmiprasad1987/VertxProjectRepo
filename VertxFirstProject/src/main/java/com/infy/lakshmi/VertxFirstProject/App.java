package com.infy.lakshmi.VertxFirstProject;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello World!");
		Vertx vertx = Vertx.vertx();
		HttpServer httpServer = vertx.createHttpServer();

		Router router = Router.router(vertx);
		// Enable multipart form data parsing
		router.route().handler(BodyHandler.create());
		
		router.route("/logon").handler(routingContext -> {
			routingContext.response().putHeader("content-type", "text/html")
					.end("<form action=\"/register\" method=\"post\">\n" + "    <div>\n"
							+ "        <label for=\"firstName\">First Name:</label>\n"
							+ "        <input type=\"text\" id=\"firstName\" name=\"firstName\" />\n" + "    </div>\n"
							+ "    <div>\n" + "        <label for=\"lastName\">Last Name:</label>\n"
							+ "        <input type=\"text\" id=\"lastName\" name=\"lastName\" />\n" + "    </div>\n"
							+ "    <div>\n" + "        <label for=\"age\">Age:</label>\n"
							+ "        <input type=\"text\" id=\"age\" name=\"age\" />\n" + "    </div>\n"
							+ "    <div>\n" + "        <label for=\"email\">Email:</label>\n"
							+ "        <input type=\"text\" id=\"email\" name=\"email\" />\n" + "    </div>\n"
							+ "    <div class=\"button\">\n" + "        <button type=\"submit\">Register</button>\n"
							+ "    </div>" + "</form>");
		});
		// handle the form
		router.post("/register").handler(ctx -> {
			ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
			// note the form attribute matches the html form element name.

			final JDBCClient client = JDBCClient.createShared(vertx,
					new JsonObject().put("url", "jdbc:mysql://localhost:3306/world")
							.put("driver_class", "com.mysql.jdbc.Driver").put("max_pool_size", 30).put("user", "root")
							.put("password", "admin"));
			JsonArray jsonArrayObject = new JsonArray();

			client.getConnection(conn -> {
				if (conn.failed()) {
					System.err.println(conn.cause().getMessage());
					return;
				}

				final SQLConnection connection = conn.result();
				// insert some test data
				String email = ctx.request().getParam("email");
				String age = ctx.request().getParam("age");
				String firstName = ctx.request().getParam("firstName");
				String lastName = ctx.request().getParam("lastName");

				connection.execute("insert into users values(" + Integer.parseInt(age) + ",'" + firstName + "','"
						+ lastName + "','" + age + "','" + email + "')", res -> {
							if (res.failed()) {
								System.out.println("failed---" + res.cause());
								throw new RuntimeException(res.cause());
							}
						});
				// and close the connection
				connection.close(done -> {
					if (done.failed()) {
						throw new RuntimeException(done.cause());
					}
				});
				// });
			});
			
		});
		
		
		router.get("/listUsers").handler(ctx -> {
		
			// note the form attribute matches the html form element name.

			final JDBCClient client = JDBCClient.createShared(vertx,
					new JsonObject().put("url", "jdbc:mysql://localhost:3306/world")
							.put("driver_class", "com.mysql.jdbc.Driver").put("max_pool_size", 30).put("user", "root")
							.put("password", "admin"));
			JsonArray jsonArrayObject = new JsonArray();
			List<Users> registeredUsersList = new ArrayList<Users>();
			

			client.getConnection(conn -> {
				if (conn.failed()) {
					System.err.println(conn.cause().getMessage());
					return;
				}

				final SQLConnection connection = conn.result();
				// query some data
				
				connection.query("select * from users", rs -> {
					for (JsonArray line : rs.result().getResults()) {
						Users u = new Users();
						u.setUserID(line.getInteger(0));
						u.setFirstName(line.getString(1));
						u.setLastName(line.getString(2));
						u.setAge(line.getString(3));
						u.setEmail(line.getString(4));
						System.out.println("Adding users");
						registeredUsersList.add(u);
						jsonArrayObject.add(line.encode());
						//System.out.println(line.encode());
						//ctx.response().write(line.encode());
					}
				});

				
				// and close the connection
				connection.close(done -> {
					if (done.failed()) {
						throw new RuntimeException(done.cause());
					}
				});
			});
			System.out.println("Finals1---" + registeredUsersList);
			ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end("Users List");
		});

		httpServer.requestHandler(router::accept).listen(8091);
	
	}
	
	
}
