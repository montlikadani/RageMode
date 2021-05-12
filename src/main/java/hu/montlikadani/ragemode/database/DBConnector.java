package hu.montlikadani.ragemode.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import hu.montlikadani.ragemode.utils.Debug;

public class DBConnector {

	private Connection connection;

	private String url, username, password, prefix;

	public DBConnector(String url, String username, String password, String prefix) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.prefix = prefix;

		getConnection();
	}

	public String getPrefix() {
		return prefix;
	}

	public Connection getConnection() {
		if (connection == null) {
			synchronized (this) {
				try {
					connection = DriverManager.getConnection(url, username, password);
				} catch (SQLException e) {
					Debug.logConsole(java.util.logging.Level.WARNING,
							"Could not connect to the database: " + e.getMessage());
				}
			}
		}

		return connection;
	}

	public boolean isValid() {
		return isValid(2);
	}

	public boolean isValid(int timeout) {
		try {
			return connection != null && connection.isValid(timeout);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean isConnected() {
		try {
			return connection != null && !connection.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
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
