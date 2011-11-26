package no.steria.kata.javaee;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;

import javax.naming.NamingException;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.hibernate.cfg.Environment;
import org.hsqldb.jdbc.JDBCDataSource;

public class WebServer {

	public static void main(String[] args) throws Exception {
		System.setProperty(Environment.HBM2DDL_AUTO, "update");
		createPersonDataSource("jdbc/personDs",
				"jdbc:hsqldb:file:target/personDs");

		attemptShutdown(8080, "sdngdslgnsdkl");
		
		Server server = new Server(8080);
		HandlerCollection handlerCollection = new HandlerCollection();
		handlerCollection.addHandler(new WebAppContext("src/main/webapp", "/"));
		handlerCollection.addHandler(new ShutdownHandler(server, "sdngdslgnsdkl"));
		server.setHandler(handlerCollection);
		server.start();
	}

	private static void createPersonDataSource(String dsName, String url)
			throws NamingException {
		JDBCDataSource ds = new JDBCDataSource();
		ds.setDatabase(url);
		ds.setUser("sa");
		ds.setPassword("");
		new EnvEntry(dsName, ds);
	}

	public static void attemptShutdown(int port, String shutdownCookie) {
		try {
			URL url = new URL("http://localhost:" + port + "/shutdown?token="
					+ shutdownCookie);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.getResponseCode();
		} catch (SocketException e) {
			// Okay - the server is not running
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
