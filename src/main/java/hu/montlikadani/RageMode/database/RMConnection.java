package hu.montlikadani.ragemode.database;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import hu.montlikadani.ragemode.Debug;

public class RMConnection {

	private Connection conn = null;

	public RMConnection(Connection conn) {
		this.conn = conn;
	}

	public Connection getConnection() {
		return conn;
	}

	public boolean isConnected() {
		try {
			return conn != null && !conn.isClosed();
		} catch (SQLException e) {
			Debug.logConsole(e.getMessage());
		}

		return false;
	}

	public boolean isValid() {
		try {
			return conn != null && conn.isValid(2);
		} catch (SQLException e) {
			Debug.logConsole(e.getMessage());
		}

		return false;
	}

	public synchronized void commit() throws SQLException {
		conn.commit();
	}

	public synchronized void executeUpdate(String query) throws SQLException {
		conn.createStatement().executeUpdate(query);
	}

	public synchronized ResultSet executeQuery(Statement statement, String query) throws SQLException {
		return statement.executeQuery(query);
	}
}
