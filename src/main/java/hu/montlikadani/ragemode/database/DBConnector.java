package hu.montlikadani.ragemode.database;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import hu.montlikadani.ragemode.utils.Debug;

public class DBConnector {

	private RMConnection conn;

	private String url, username, password, prefix;

	public DBConnector(String url, String username, String password, String prefix) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.prefix = prefix;

		// Connect to the db
		getConnection();
	}

	public String getPrefix() {
		return prefix;
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

	public synchronized <T> CompletableFuture<T> dispatchAsync(Callable<T> task) {
		CompletableFuture<T> future = new CompletableFuture<>();

		try {
			future.complete(task.call());
		} catch (Exception e) {
			future.completeExceptionally(e);
		}

		return future;
	}
}
