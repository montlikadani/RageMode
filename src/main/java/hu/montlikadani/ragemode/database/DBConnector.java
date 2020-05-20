package hu.montlikadani.ragemode.database;

import java.sql.DriverManager;
import java.sql.SQLException;

import hu.montlikadani.ragemode.Debug;

public class DBConnector {

	private RMConnection conn = null;

	private String url;
	private String username;
	private String password;
	private String prefix;

	public DBConnector(String url, String username, String password, String prefix) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.prefix = prefix;

		// Connect to the db
		getConnection();
	}

	public RMConnection getConnection() {
		if (conn == null) {
			synchronized (this) {
				try {
					conn = new RMConnection(DriverManager.getConnection(url, username, password));
				} catch (SQLException e) {
					Debug.logConsole(java.util.logging.Level.WARNING,
							"Could not connect to the database: " + e.getMessage());
				}
			}
		}

		return conn;
	}

	public boolean isValid() {
		return isValid(2);
	}

	public boolean isValid(int timeout) {
		return conn != null && getConnection().isValid(timeout);
	}

	public boolean isConnected() {
		return conn != null && getConnection().isConnected();
	}

	public String getPrefix() {
		return prefix;
	}
}
