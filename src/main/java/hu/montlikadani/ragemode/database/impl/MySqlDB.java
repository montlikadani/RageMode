package hu.montlikadani.ragemode.database.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.google.common.collect.ImmutableList;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.database.DB;
import hu.montlikadani.ragemode.database.DBType;
import hu.montlikadani.ragemode.database.Database;
import hu.montlikadani.ragemode.database.connector.MySQLConnect;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.utils.Debug;
import hu.montlikadani.ragemode.utils.ReJoinDelay;

@DB(type = DBType.MYSQL, fileBased = false)
public class MySqlDB implements Database {

	private final RageMode rm = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	private MySQLConnect connect;

	@Override
	public DBType getDatabaseType() {
		return DBType.MYSQL;
	}

	@Override
	public MySQLConnect getDatabase() {
		return connect;
	}

	@Override
	public void loadPlayerStatistics() {
		if (!connect.isConnected() || !connect.isValid()) {
			return;
		}

		connect.dispatchAsync(() -> {
			int totalPlayers = 0, kills = 0, axeKills = 0, directArrowKills = 0, explosionKills = 0, knifeKills = 0,
					zombieKills = 0, deaths = 0, axeDeaths = 0, directArrowDeaths = 0, explosionDeaths = 0,
					knifeDeaths = 0, currentWins = 0, currentScore = 0, currentGames = 0;

			double kd = 0d;

			try (Connection conn = connect.getConnection();
					Statement statement = conn.createStatement();
					ResultSet rs = statement
							.executeQuery("SELECT * FROM `" + connect.getPrefix() + "stats_players`;")) {
				while (rs.next()) {
					String stringId = rs.getString("uuid");

					if (stringId == null) {
						continue;
					}

					UUID uuid;

					try {
						uuid = UUID.fromString(stringId);
					} catch (IllegalArgumentException e) {
						continue;
					}

					PlayerPoints rPP = RuntimePPManager.getPPForPlayer(uuid);
					if (rPP == null) {
						rPP = new PlayerPoints(uuid);
					}

					kills = rs.getInt("kills");
					axeKills = rs.getInt("axe_kills");
					directArrowKills = rs.getInt("direct_arrow_kills");
					explosionKills = rs.getInt("explosion_kills");
					knifeKills = rs.getInt("knife_kills");
					zombieKills = rs.getInt("zombie_kills");

					deaths = rs.getInt("deaths");
					axeDeaths = rs.getInt("axe_deaths");
					directArrowDeaths = rs.getInt("direct_arrow_deaths");
					explosionDeaths = rs.getInt("explosion_deaths");
					knifeDeaths = rs.getInt("knife_deaths");

					kd = rs.getDouble("kd");

					currentWins = rs.getInt("wins");
					currentGames = rs.getInt("games");
					currentScore = rs.getInt("score");

					rPP.setAxeDeaths(axeDeaths);
					rPP.setAxeKills(axeKills);
					rPP.setDeaths(deaths);
					rPP.setDirectArrowDeaths(directArrowDeaths);
					rPP.setDirectArrowKills(directArrowKills);
					rPP.setExplosionDeaths(explosionDeaths);
					rPP.setExplosionKills(explosionKills);
					rPP.setKills(kills);
					rPP.setKnifeDeaths(knifeDeaths);
					rPP.setKnifeKills(knifeKills);
					rPP.setZombieKills(zombieKills);

					rPP.setKD(kd);

					rPP.setWins(currentWins);
					rPP.setPoints(currentScore);
					rPP.setGames(currentGames);

					totalPlayers++;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return totalPlayers;
		}).thenAccept(total -> {
			if (total > 0) {
				Debug.logConsole("Loaded {0} player{1} stats database.", total, (total > 1 ? "s" : ""));
			}
		});
	}

	@Override
	public void saveAllPlayerData() {
		RuntimePPManager.getRuntimePPList().forEach(this::addPlayerStatistics);
	}

	@Override
	public void addPlayerStatistics(PlayerPoints playerPoints) {
		if (!connect.isValid()) {
			return;
		}

		connect.dispatchAsync(() -> {
			try (Connection conn = connect.getConnection();
					PreparedStatement prestt = conn.prepareStatement("REPLACE INTO `" + connect.getPrefix()
							+ "stats_players` (name, uuid, kills, axe_kills, direct_arrow_kills, explosion_kills,"
							+ " knife_kills, zombie_kills, deaths, axe_deaths, direct_arrow_deaths, explosion_deaths,"
							+ " knife_deaths, wins, score, games, kd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
				prestt.setString(1, Bukkit.getOfflinePlayer(playerPoints.getUUID()).getName());
				prestt.setString(2, playerPoints.toStringUUID());

				int deaths = playerPoints.getDeaths();
				double newKD = deaths > 0 ? (((double) playerPoints.getKills()) / ((double) deaths)) : 1;

				prestt.setInt(3, playerPoints.getKills());
				prestt.setInt(4, playerPoints.getAxeKills());
				prestt.setInt(5, playerPoints.getDirectArrowKills());
				prestt.setInt(6, playerPoints.getExplosionKills());
				prestt.setInt(7, playerPoints.getKnifeKills());
				prestt.setInt(8, playerPoints.getZombieKills());
				prestt.setInt(9, deaths);
				prestt.setInt(10, playerPoints.getAxeDeaths());
				prestt.setInt(11, playerPoints.getDirectArrowDeaths());
				prestt.setInt(12, playerPoints.getExplosionDeaths());
				prestt.setInt(13, playerPoints.getKnifeDeaths());
				prestt.setInt(14, playerPoints.getWins());
				prestt.setInt(15, playerPoints.getPoints());
				prestt.setInt(16, playerPoints.getGames());
				prestt.setDouble(17, newKD);

				prestt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return null;
		});
	}

	@Override
	public void addPoints(int points, UUID uuid) {
		if (!connect.isValid()) {
			return;
		}

		connect.dispatchAsync(() -> {
			String stringId = uuid.toString();
			int oldPoints = 0;

			try (Connection conn = connect.getConnection()) {
				try (Statement statement = conn.createStatement();
						ResultSet rs = statement.executeQuery("SELECT * FROM `" + connect.getPrefix()
								+ "stats_players` WHERE `uuid` LIKE '" + stringId + "';")) {
					if (rs.next()) {
						oldPoints = rs.getInt("score");
					}
				}

				try (PreparedStatement prestt = conn.prepareStatement("REPLACE INTO `" + connect.getPrefix()
						+ "stats_players` (name, uuid, score) VALUES (?, ?, ?);")) {
					prestt.setString(1, Bukkit.getOfflinePlayer(uuid).getName());
					prestt.setString(2, stringId);
					prestt.setInt(3, oldPoints + points);
					prestt.executeUpdate();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return null;
		});
	}

	@Override
	public ImmutableList<PlayerPoints> getAllPlayerStatistics() {
		if (!connect.isValid()) {
			return ImmutableList.of();
		}

		return connect.dispatchAsync(() -> {
			final List<PlayerPoints> allRPPs = new ArrayList<>();

			try (Connection conn = connect.getConnection();
					Statement statement = conn.createStatement();
					ResultSet rs = statement
							.executeQuery("SELECT * FROM `" + connect.getPrefix() + "stats_players`;")) {
				while (rs.next()) {
					String stringId = rs.getString("uuid");

					if (stringId != null) {
						UUID uuid;

						try {
							uuid = UUID.fromString(stringId);
						} catch (IllegalArgumentException e) {
							continue;
						}

						PlayerPoints pp = RuntimePPManager.getPPForPlayer(uuid);
						if (pp == null) {
							pp = new PlayerPoints(uuid);
						}

						allRPPs.add(pp);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return ImmutableList.copyOf(allRPPs);
		}).join();
	}

	@Override
	public PlayerPoints getPlayerStatsFromData(UUID uuid) {
		if (!connect.isValid()) {
			return null;
		}

		return connect.dispatchAsync(() -> {
			PlayerPoints pp = RuntimePPManager.getPPForPlayer(uuid);

			if (pp == null) {
				return null;
			}

			// Cloning to ignore overwrite
			pp = (PlayerPoints) pp.clone();

			try (Connection conn = connect.getConnection();
					Statement statement = conn.createStatement();
					ResultSet rs = statement.executeQuery("SELECT * FROM `" + connect.getPrefix()
							+ "stats_players` WHERE `uuid` LIKE '" + uuid.toString() + "';")) {
				if (rs.next()) {
					pp.setKills(rs.getInt("kills"));
					pp.setAxeKills(rs.getInt("axe_kills"));
					pp.setDirectArrowKills(rs.getInt("direct_arrow_kills"));
					pp.setExplosionKills(rs.getInt("explosion_kills"));
					pp.setKnifeKills(rs.getInt("knife_kills"));
					pp.setZombieKills(rs.getInt("zombie_kills"));

					pp.setDeaths(rs.getInt("deaths"));
					pp.setAxeDeaths(rs.getInt("axe_deaths"));
					pp.setDirectArrowDeaths(rs.getInt("direct_arrow_deaths"));
					pp.setExplosionDeaths(rs.getInt("explosion_deaths"));
					pp.setKnifeDeaths(rs.getInt("knife_deaths"));

					pp.setWins(rs.getInt("wins"));
					pp.setPoints(rs.getInt("score"));
					pp.setGames(rs.getInt("games"));
					pp.setKD(rs.getDouble("kd"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}

			return pp;
		}).join();
	}

	@Override
	public boolean resetPlayerStatistic(UUID uuid) {
		final PlayerPoints rpp = RuntimePPManager.getPPForPlayer(uuid);
		if (rpp == null) {
			return false;
		}

		rpp.setKills(0);
		rpp.setAxeKills(0);
		rpp.setDirectArrowKills(0);
		rpp.setExplosionKills(0);
		rpp.setKnifeKills(0);
		rpp.setZombieKills(0);

		rpp.setDeaths(0);
		rpp.setAxeDeaths(0);
		rpp.setDirectArrowDeaths(0);
		rpp.setExplosionDeaths(0);
		rpp.setKnifeDeaths(0);

		rpp.setWins(0);
		rpp.setPoints(0);
		rpp.setGames(0);
		rpp.setKD(0d);

		if (!connect.isConnected()) {
			return false;
		}

		return connect.dispatchAsync(() -> {
			try (Connection conn = connect.getConnection();
					PreparedStatement prestt = conn.prepareStatement("REPLACE INTO `" + connect.getPrefix()
							+ "stats_players` (name, uuid, kills, axe_kills, direct_arrow_kills, explosion_kills,"
							+ " knife_kills, zombie_kills, deaths, axe_deaths, direct_arrow_deaths, explosion_deaths,"
							+ " knife_deaths, wins, score, games, kd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
				prestt.setString(1, Bukkit.getOfflinePlayer(rpp.getUUID()).getName());
				prestt.setString(2, rpp.toStringUUID());

				for (int i = 3; i <= 16; i++) {
					prestt.setInt(i, 0);
				}

				prestt.setDouble(16, 0d);
				prestt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return true;
		}).join();
	}

	@Override
	public void connectDatabase() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException c) {
				c.printStackTrace();
				Debug.logConsole(Level.WARNING, "Could not connect to the MySQL database. No MySql driver found.");
				return;
			}
		}

		Debug.logConsole("Attempting to connect to mysql...");

		if ((connect = new MySQLConnect(ConfigValues.getMySqlDatabaseConnectionCommand(),
				ConfigValues.getMysqlUsername(), ConfigValues.getMysqlPassword(),
				ConfigValues.getDatabaseTablePrefix())).isConnected()) {
			Debug.logConsole("Connected to MySQL!");
		}
	}

	@Override
	public void loadDatabase(boolean startup) {
		if (connect == null || !connect.isConnected()) {
			connectDatabase();
		}

		RuntimePPManager.loadPPListFromDatabase(rm.getDatabase());
		loadPlayerStatistics();

		if (startup) {
			loadMiscPlayersData();
		}
	}

	@Override
	public void saveDatabase() {
		saveAllPlayerData();
		saveMiscPlayersData();
	}

	@Override
	public CompletableFuture<Boolean> convertDatabase(final String type) {
		return CompletableFuture.supplyAsync(() -> {
			if (getDatabaseType().toString().equalsIgnoreCase(type)) {
				return false;
			}

			if (connect != null && connect.isConnected()) {
				try {
					connect.getConnection().close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			ConfigValues.databaseType = type;

			rm.getConfig().set("database.type", type);
			Configuration.saveFile(rm.getConfig(), rm.getConfiguration().getCfgFile());

			rm.connectDatabase(true);

			RuntimePPManager.getRuntimePPList().forEach(rm.getDatabase()::addPlayerStatistics);
			RuntimePPManager.loadPPListFromDatabase(rm.getDatabase());
			return false;
		});
	}

	@Override
	public void loadMiscPlayersData() {
		if (!connect.isConnected()) {
			return;
		}

		connect.dispatchAsync(() -> {
			try (Connection conn = connect.getConnection()) {
				try (Statement statement = conn.createStatement();
						ResultSet rs = statement.executeQuery("SELECT * FROM `" + connect.getPrefix() + "players`;")) {
					while (rs.next()) {
						String stringUuId = rs.getString("uuid");

						if (stringUuId != null) {
							UUID uuid;

							try {
								uuid = UUID.fromString(stringUuId);
							} catch (IllegalArgumentException e) {
								continue;
							}

							if (rs.getBoolean("deathMessagesEnabled")) {
								PlayerManager.DEATH_MESSAGES_TOGGLE.put(uuid, true);
							}

							if (ConfigValues.isRejoinDelayEnabled() && ConfigValues.isRememberRejoinDelay()) {
								ReJoinDelay.setTime(Bukkit.getOfflinePlayer(uuid), rs.getLong("time"));
							}
						}
					}
				}

				try (PreparedStatement ps = conn.prepareStatement(
						"ALTER TABLE `" + connect.getPrefix() + "players` DROP COLUMN deathMessagesEnabled;")) {
					ps.executeUpdate();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return null;
		});
	}

	@Override
	public void saveMiscPlayersData() {
		if (!ConfigValues.isRejoinDelayEnabled() && !ConfigValues.isRememberRejoinDelay()
				&& PlayerManager.DEATH_MESSAGES_TOGGLE.isEmpty()) {
			return;
		}

		if (!connect.isConnected()) {
			return;
		}

		connect.dispatchAsync(() -> {
			try {
				try (Connection conn = connect.getConnection()) {
					if (ConfigValues.isRejoinDelayEnabled() && ConfigValues.isRememberRejoinDelay()) {
						Map<OfflinePlayer, Long> map = ReJoinDelay.getPlayerTimes();

						if (!map.isEmpty()) {
							try (PreparedStatement prestt = conn.prepareStatement(
									"REPLACE INTO `" + connect.getPrefix() + "players` (uuid, time) VALUES (?, ?);")) {
								for (Map.Entry<OfflinePlayer, Long> m : map.entrySet()) {
									if (m.getValue().longValue() > System.currentTimeMillis()) {
										prestt.setString(1, m.getKey().getUniqueId().toString());
										prestt.setLong(2, m.getValue());
										prestt.executeUpdate();
									}
								}
							}
						}
					}

					if (!PlayerManager.DEATH_MESSAGES_TOGGLE.isEmpty()) {
						try (PreparedStatement prestt = conn.prepareStatement("REPLACE INTO `" + connect.getPrefix()
								+ "players` (uuid, deathMessagesEnabled) VALUES (?, ?);")) {

							for (Map.Entry<UUID, Boolean> map : PlayerManager.DEATH_MESSAGES_TOGGLE.entrySet()) {
								if (map.getValue()) {
									prestt.setString(1, map.getKey().toString());
									prestt.setBoolean(2, true);
									prestt.executeUpdate();
								}
							}
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return null;
		});
	}
}