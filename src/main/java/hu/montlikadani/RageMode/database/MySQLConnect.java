package hu.montlikadani.ragemode.database;

import java.sql.SQLException;

public class MySQLConnect extends Database {

	public MySQLConnect(String host, String port, String database, String userName, String password,
			boolean serverCertificate, boolean useUnicode, String characterEncode, boolean autoReconnect,
			boolean useSSL, String prefix) {
		super("jdbc:mysql://" + host + ":" + port + "/" + database + "?verifyServerCertificate=" + serverCertificate
				+ "&useUnicode=" + useUnicode + "&characterEncoding=" + characterEncode + "&autoReconnect="
				+ autoReconnect + "&useSSL=" + useSSL, userName, password, prefix);

		createDefaultTable();
	}

	public void createDefaultTable() {
		try {
			if (getConnection() != null && getConnection().isConnected()) {
				String query = "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "stats_players` (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , name VARCHAR(255) , uuid VARCHAR(255) , kills INT(11) , axe_kills INT(11) , direct_arrow_kills INT(11) , explosion_kills INT(11) , knife_kills INT(11) , deaths INT(11) , axe_deaths INT(11) , direct_arrow_deaths INT(11) , explosion_deaths INT(11) , knife_deaths INT(11) , wins INT(11) , score INT(11) , games INT(11) , kd DOUBLE, UNIQUE(uuid));";
				getConnection().executeUpdate(query);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
