package hu.montlikadani.ragemode.database;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

	private RMConnection conn = null;

	private String url;
	private String username;
	private String password;
	private String prefix;

	public Database(String url, String username, String password, String prefix) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.prefix = prefix;
	}

	public RMConnection getConnection() {
		if (conn == null) {
			try {
				conn = new RMConnection(DriverManager.getConnection(url, username, password));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return conn;
	}

	public String getPrefix() {
		return prefix;
	}
}
