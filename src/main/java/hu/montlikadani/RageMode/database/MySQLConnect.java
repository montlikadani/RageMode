package hu.montlikadani.ragemode.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.Validate;

import hu.montlikadani.ragemode.Debug;

public class MySQLConnect extends DBConnector implements DBMethods {

	public MySQLConnect(String host, String port, String database, String userName, String password,
			boolean serverCertificate, boolean useUnicode, String charEncode, boolean autoReconnect,
			boolean useSSL, String prefix) {
		super("jdbc:mysql://" + host + ":" + port + "/" + database + "?verifyServerCertificate=" + serverCertificate
				+ "&maxReconnects=1&useUnicode=" + useUnicode + "&characterEncoding=" + charEncode + "&autoReconnect="
				+ autoReconnect + "&useSSL=" + useSSL, userName, password, prefix);

		createStatsTable();
	}

	public void createStatsTable() {
		if (isConnected()) {
			String query = "CREATE TABLE IF NOT EXISTS `" + getPrefix()
					+ "stats_players` (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , `name` VARCHAR(255) , `uuid` VARCHAR(255) , "
					+ "`kills` INTEGER , `axe_kills` INTEGER , `direct_arrow_kills` INTEGER , `explosion_kills` INTEGER , "
					+ "`knife_kills` INTEGER , `deaths` INTEGER , `axe_deaths` INTEGER , `direct_arrow_deaths` INTEGER , "
					+ "`explosion_deaths` INTEGER , `knife_deaths` INTEGER , `wins` INTEGER , `score` INTEGER , `games` INTEGER , "
					+ "`kd` DOUBLE, UNIQUE(uuid));";
			createTable(query);
		}
	}

	@Override
	public boolean createTable(String query) {
		Validate.notNull(query, "The query can't be null!");
		Validate.notEmpty(query, "The query can't be empty!");

		if (isConnected()) {
			try {
				getConnection().executeUpdate(query);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public boolean truncate(String table) {
		Validate.notNull(table, "The table name can't be null!");
		Validate.notEmpty(table, "The table name can't be empty!");

		if (!isConnected()) {
			return false;
		}

		if (!isTable(table)) {
			Debug.logConsole("Table {0} does not exists!", table);
			return false;
		}

		try {
			getConnection().executeUpdate("DELETE FROM " + table + ";");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean drop(String table) {
		Validate.notNull(table, "The table name can't be null!");
		Validate.notEmpty(table, "The table name can't be empty!");

		if (isConnected()) {
			try {
				getConnection().executeUpdate("DROP TABLE IF EXISTS `" + table + "`;");
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public boolean isTable(String name) {
		Validate.notNull(name, "The table name can't be null!");
		Validate.notEmpty(name, "The table name can't be empty!");

		if (isConnected()) {
			try {
				ResultSet tables = getConnection().getConnection().getMetaData().getTables(null, null, name, null);
				if (tables.next()) {
					tables.close();
					return true;
				}

				tables.close();
			} catch (SQLException e) {
				Debug.logConsole("Table with name {0} does not exists or not a table!", name);
			}
		}
		return false;
	}

	@Override
	public boolean isCollumn(String table, String collumn) {
		Validate.notNull(table, "The table name can't be null!");
		Validate.notNull(collumn, "The collumn can't be null!");
		Validate.notEmpty(table, "The table name can't be empty!");
		Validate.notEmpty(collumn, "The collumn can't be empty!");

		if (isConnected()) {
			try {
				getConnection().executeQuery(getConnection().createStatement(), "SELECT `" + collumn + "` FROM `" + table + "`;");
				return true;
			} catch (SQLException e) {
				Debug.logConsole("Column {0} with table {1} not a collumn or does not exists.", collumn, table);
			}
		}
		return false;
	}
}
