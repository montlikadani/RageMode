package hu.montlikadani.ragemode.database;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.Validate;

import hu.montlikadani.ragemode.Debug;

public class SQLConnect extends DBConnector implements DBInterfaces {

	public SQLConnect(File file, String prefix) {
		super("jdbc:sqlite:" + file, null, null, prefix);

		createDefaultTable();
	}

	public void createDefaultTable() {
		if (hasConnection()) {
			String query = "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "stats_players` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(255) , uuid VARCHAR(255) , kills INT(11) , axe_kills INT(11) , direct_arrow_kills INT(11) , explosion_kills INT(11) , knife_kills INT(11) , deaths INT(11) , axe_deaths INT(11) , direct_arrow_deaths INT(11) , explosion_deaths INT(11) , knife_deaths INT(11) , wins INT(11) , score INT(11) , games INT(11) , kd DOUBLE, UNIQUE(uuid));";
			createTable(query);
		}
	}

	@Override
	public boolean hasConnection() {
		return getConnection() != null && getConnection().isConnected();
	}

	@Override
	public boolean createTable(String query) {
		Validate.notNull(query, "The query can't be null!");
		Validate.notEmpty(query, "The query can't be empty!");

		if (hasConnection()) {
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

		if (!isTable(table)) {
			Debug.logConsole("Table {0} does not exists!", table);
			return false;
		}

		if (!hasConnection()) {
			return false;
		}

		String query = "DELETE FROM " + table + ";";
		try {
			getConnection().executeUpdate(query);
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

		if (hasConnection()) {
			String query = "DROP TABLE IF EXISTS `" + table + "`;";
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
	public boolean isTable(String name) {
		Validate.notNull(name, "The table name can't be null!");
		Validate.notEmpty(name, "The table name can't be empty!");

		if (hasConnection()) {
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

		if (hasConnection()) {
			try {
				ResultSet tables = getConnection().getConnection().getMetaData().getColumns(null, null, table, collumn);
				if (tables.next()) {
					tables.close();
					return true;
				}

				tables.close();
			} catch (SQLException e) {
				Debug.logConsole("Could not check if table {0} exists, SQLException: {1}", table, e.getMessage());
			}
		}

		return false;
	}
}
