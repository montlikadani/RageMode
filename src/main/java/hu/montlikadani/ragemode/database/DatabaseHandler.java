package hu.montlikadani.ragemode.database;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.storage.MySQLDB;
import hu.montlikadani.ragemode.storage.SQLDB;
import hu.montlikadani.ragemode.storage.YAMLDB;

public class DatabaseHandler {

	private RageMode plugin;

	private DBType type = DBType.YAML;

	private static DBConnector connector;

	public DatabaseHandler(RageMode plugin) {
		this.plugin = plugin;
	}

	public void setDatabaseType(DBType type) {
		this.type = type;
	}

	public void setDatabaseType(String name) {
		this.type = DBType.valueOf(name.toUpperCase());
	}

	public DBType getDBType() {
		return type;
	}

	/**
	 * Gets the {@link MySQLConnect} class
	 * @return mySQLConnect class
	 */
	public static MySQLConnect getMySQL() {
		return (MySQLConnect) connector;
	}

	/**
	 * Gets the {@link SQLConnect} class
	 * @return SQLConnect class
	 */
	public static SQLConnect getSQL() {
		return (SQLConnect) connector;
	}

	public void connectDatabase() {
		String prefix = ConfigValues.getDatabaseTablePrefix();
		if (prefix.isEmpty()) {
			prefix = "rm_";
		}

		switch (type) {
		case MYSQL:
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException c) {
				c.printStackTrace();
				Debug.logConsole(Level.WARNING, "Could not connect to the MySQL database. No MySql found.");
				break;
			}

			String host = ConfigValues.getHost(),
					port = ConfigValues.getPort(),
					database = ConfigValues.getDatabase(),
					username = ConfigValues.getUsername(),
					password = ConfigValues.getPassword(),
					charEncode = ConfigValues.getEncoding();

			boolean serverCertificate = ConfigValues.isCertificate(),
					useUnicode = ConfigValues.isUnicode(),
					autoReconnect = ConfigValues.isAutoReconnect(),
					useSSL = ConfigValues.isUseSSL();

			if (charEncode.isEmpty()) {
				charEncode = java.nio.charset.StandardCharsets.UTF_8.name();
			}

			if ((connector = new MySQLConnect(host, port, database, username, password, serverCertificate, useUnicode,
					charEncode, autoReconnect, useSSL, prefix)).isConnected()) {
				Debug.logConsole("Successfully connected to MySQL!");
			}

			break;
		case SQLITE:
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException c) {
				c.printStackTrace();
				Debug.logConsole(Level.WARNING, "Could not connect to the SQL database. No Sql found.");
				break;
			}

			File sqlFile = new File(plugin.getFolder(), ConfigValues.getSqlFileName() + ".db");
			if (!sqlFile.exists()) {
				try {
					sqlFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if ((connector = new SQLConnect(sqlFile, prefix)).isConnected()) {
				Debug.logConsole("Successfully connected to SQL!");
			}

			break;
		default:
			break;
		}
	}

	public void loadDatabase(boolean startup) {
		switch (type) {
		case MYSQL:
			if (connector == null || !connector.isConnected()) {
				connectDatabase();
			}

			RuntimePPManager.loadPPListFromDatabase();
			MySQLDB.loadPlayerStatistics();

			if (startup) {
				MySQLDB.loadJoinDelay();
			}

			break;
		case SQLITE:
			if (connector == null || !connector.isConnected()) {
				connectDatabase();
			}

			RuntimePPManager.loadPPListFromDatabase();
			SQLDB.loadPlayerStatistics();

			if (startup) {
				SQLDB.loadJoinDelay();
			}

			break;
		case YAML:
			YAMLDB.initFile();
			RuntimePPManager.loadPPListFromDatabase();
			YAMLDB.loadPlayerStatistics();

			if (startup) {
				YAMLDB.loadJoinDelay();
			}

			break;
		default:
			break;
		}
	}

	public void saveDatabase() {
		if (!ConfigValues.isRejoinDelayEnabled() || !ConfigValues.isRememberRejoinDelay()) {
			return;
		}

		switch (type) {
		case YAML:
			YAMLDB.saveData();
			YAMLDB.saveJoinDelay();
			break;
		case SQLITE:
			SQLDB.saveData();
			SQLDB.saveJoinDelay();
			break;
		case MYSQL:
			MySQLDB.saveData();
			MySQLDB.saveJoinDelay();
			break;
		default:
			break;
		}
	}

	public boolean convertDatabase(final String type) {
		if (connector != null && connector.isConnected()) {
			try {
				connector.getConnection().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		setDatabaseType(type);
		connectDatabase();
		loadDatabase(false);

		for (Player players : Bukkit.getOnlinePlayers()) {
			final PlayerPoints pp = RuntimePPManager.getPPForPlayer(players.getUniqueId());
			if (pp == null) {
				continue;
			}

			switch (this.type) {
			case MYSQL:
				MySQLDB.addPlayerStatistics(pp);
				break;
			case SQLITE:
				SQLDB.addPlayerStatistics(pp);
				break;
			case YAML:
				YAMLDB.addPlayerStatistics(pp);
				break;
			default:
				break;
			}
		}

		RuntimePPManager.loadPPListFromDatabase();
		return true;
	}
}
