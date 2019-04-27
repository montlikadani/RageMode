package hu.montlikadani.ragemode.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import hu.montlikadani.ragemode.RageMode;

public class MySQLConnect {

	private Connection connection = null;

	public MySQLConnect(String host, String port, String database, String userName, String password, boolean serverCertificate,
			boolean useUnicode, String characterEncode, boolean autoReconnect, boolean useSSL) {
		Connection connection = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");
			try {
				String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?verifyServerCertificate=" + serverCertificate
						+ "&useUnicode=" + useUnicode + "&characterEncoding=" + characterEncode + "&autoReConnect="
						+ autoReconnect + "&useSSL=" + useSSL;
				connection = DriverManager.getConnection(url, userName, password);
			} catch (SQLException e) {
				RageMode.logConsole(Level.WARNING, "Could not connect to the MySQL database.");
				return;
			}
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			RageMode.logConsole(Level.WARNING, "Could not connect to the MySQL database.");
			return;
		}

		this.connection = connection;

		createDefaultTable();
	}

	public void createDefaultTable() {
		try {
			if (connection != null && !connection.isClosed()) {
				Statement statement = null;
				String query = "CREATE TABLE IF NOT EXISTS `rm_stats_players` (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , name VARCHAR(255) , uuid VARCHAR(255) , kills INT(11) , axe_kills INT(11) , direct_arrow_kills INT(11) , explosion_kills INT(11) , knife_kills INT(11) , deaths INT(11) , axe_deaths INT(11) , direct_arrow_deaths INT(11) , explosion_deaths INT(11) , knife_deaths INT(11) , wins INT(11) , score INT(11) , games INT(11) , kd DOUBLE, UNIQUE(uuid));";
				statement = connection.createStatement();
				statement.executeUpdate(query);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return connection;
	}
}
