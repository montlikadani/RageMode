package hu.montlikadani.ragemode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;

public class MySQLConnect {

	private Connection connection = null;

	public MySQLConnect(String host, String port, String database, String userName, String password) {
		Connection connection = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", userName);
		connectionProps.put("password", password);

		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, connectionProps);
		} catch (SQLException e) {
			RageMode.logConsole(Level.WARNING, "Could not connect to the MySQL database.");
			return;
		}

		this.connection = connection;

		createDefaultTable();
	}

	public void createDefaultTable() {
		if (connection != null) {
			Statement statement = null;
			String query = "CREATE TABLE IF NOT EXISTS `rm_stats_players` (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , name VARCHAR(255) , uuid VARCHAR(255) , kills INT(11) , axe_kills INT(11) , direct_arrow_kills INT(11) , explosion_kills INT(11) , knife_kills INT(11) , deaths INT(11) , axe_deaths INT(11) , direct_arrow_deaths INT(11) , explosion_deaths INT(11) , knife_deaths INT(11) , wins INT(11) , score INT(11) , games INT(11) , kd DOUBLE, UNIQUE(uuid));";
			try {
				statement = connection.createStatement();
				statement.executeUpdate(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Connection getConnection() {
		return connection;
	}
}