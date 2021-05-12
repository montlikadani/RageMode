package hu.montlikadani.ragemode.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang.Validate;

import hu.montlikadani.ragemode.utils.Debug;

public class MySQLConnect extends DBConnector implements DBMethods {

	public MySQLConnect(String host, String port, String database, String userName, String password,
			boolean serverCertificate, boolean useUnicode, String charEncode, boolean autoReconnect, boolean useSSL,
			String prefix) {
		super("jdbc:mysql://" + host + ":" + port + "/" + database + "?verifyServerCertificate=" + serverCertificate
				+ "&maxReconnects=1&useUnicode=" + useUnicode + "&characterEncoding=" + charEncode + "&autoReconnect="
				+ autoReconnect + "&useSSL=" + useSSL, userName, password, prefix);

		if (isConnected()) {
			createStatsTable();
			createPlayersTable();
		}
	}

	private void createStatsTable() {
		createTable("CREATE TABLE IF NOT EXISTS `" + getPrefix()
				+ "stats_players` (`id` INT AUTO_INCREMENT PRIMARY KEY, `name` VARCHAR(255) NOT NULL, `uuid` VARCHAR(255) NOT NULL, "
				+ "`kills` INTEGER, `axe_kills` INTEGER, `direct_arrow_kills` INTEGER, `explosion_kills` INTEGER, "
				+ "`knife_kills` INTEGER, `zombie_kills` INTEGER, `deaths` INTEGER, `axe_deaths` INTEGER, `direct_arrow_deaths` INTEGER, "
				+ "`explosion_deaths` INTEGER, `knife_deaths` INTEGER, `wins` INTEGER, `score` INTEGER, `games` INTEGER, "
				+ "`kd` DOUBLE, UNIQUE(uuid));");
	}

	private void createPlayersTable() {
		createTable("CREATE TABLE IF NOT EXISTS `" + getPrefix() + "players` (`id` INT AUTO_INCREMENT PRIMARY KEY, "
				+ "`uuid` VARCHAR(255) NOT NULL, `time` LONG, `deathMessagesEnabled` BOOLEAN, UNIQUE(uuid));");
	}

	@Override
	public boolean createTable(String query) {
		Validate.notEmpty(query, "The query can't be empty/null");

		return dispatchAsync(() -> {
			if (isConnected()) {
				try (Statement statement = getConnection().createStatement()) {
					statement.executeUpdate(query);
					return true;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			return false;
		}).join();
	}

	@Override
	public boolean truncate(String table) {
		Validate.notEmpty(table, "The table name can't be empty/null");

		if (!isConnected()) {
			return false;
		}

		if (!isTable(table)) {
			Debug.logConsole("Table {0} does not exists!", table);
			return false;
		}

		return dispatchAsync(() -> {
			try (Statement statement = getConnection().createStatement()) {
				statement.executeUpdate("DELETE FROM " + table + ";");
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return false;
		}).join();
	}

	@Override
	public boolean drop(String table) {
		Validate.notEmpty(table, "The table name can't be empty/null");

		return dispatchAsync(() -> {
			if (isConnected()) {
				try (Statement statement = getConnection().createStatement()) {
					statement.executeUpdate("DROP TABLE IF EXISTS `" + table + "`;");
					return true;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			return false;
		}).join();
	}

	@Override
	public boolean isTable(String name) {
		Validate.notEmpty(name, "The table name can't be empty/null");

		return dispatchAsync(() -> {
			if (isConnected()) {
				try (ResultSet tables = getConnection().getMetaData().getTables(null, null, name, null)) {
					if (tables.next()) {
						return true;
					}
				} catch (SQLException e) {
					Debug.logConsole("Table with name {0} does not exists or not a table!", name);
				}
			}

			return false;
		}).join();
	}

	@Override
	public boolean isCollumn(String table, String collumn) {
		Validate.notEmpty(table, "The table name can't be empty/null");
		Validate.notEmpty(collumn, "The collumn can't be empty/null");

		return dispatchAsync(() -> {
			if (isConnected()) {
				try (Statement statement = getConnection().createStatement()) {
					statement.executeQuery("SELECT `" + collumn + "` FROM `" + table + "`;");
					return true;
				} catch (SQLException e) {
					Debug.logConsole("Column {0} with table {1} not a collumn or does not exists.", collumn, table);
				}
			}

			return false;
		}).join();
	}
}
