package hu.montlikadani.ragemode.storage;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.database.DB;
import hu.montlikadani.ragemode.database.DBType;
import hu.montlikadani.ragemode.database.Database;
import hu.montlikadani.ragemode.database.RMConnection;
import hu.montlikadani.ragemode.database.SQLConnect;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.utils.ReJoinDelay;

@DB(type = DBType.SQLITE)
public class SQLDB implements Database {

	private SQLConnect connect;

	@Override
	public SQLConnect getDatabase() {
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

			String query = "SELECT * FROM `" + connect.getPrefix() + "stats_players`;";
			RMConnection conn = connect.getConnection();
			Statement statement = null;
			try {
				statement = conn.createStatement();
				ResultSet rs = conn.executeQuery(statement, query);
				while (rs.next()) {
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

					String playerUUID = rs.getString("uuid");
					UUID uuid = UUID.fromString(playerUUID);
					PlayerPoints rPP = RuntimePPManager.getPPForPlayer(uuid);
					if (rPP == null) {
						rPP = new PlayerPoints(uuid);
					}

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

				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				// Do not close the connection to prevent loading issues
			}

			return totalPlayers;
		}).thenAccept(total -> {
			if (total > 0) {
				Debug.logConsole("Loaded {0} player{1} stats database.", total, (total > 1 ? "s" : ""));
			}
		});
	}

	@Override
	public void saveData() {
		RuntimePPManager.getRuntimePPList().forEach(this::addPlayerStatistics);
	}

	@Override
	public void addPlayerStatistics(PlayerPoints playerPoints) {
		if (!connect.isValid()) {
			return;
		}

		connect.dispatchAsync(() -> {
			String query = "SELECT * FROM `" + connect.getPrefix() + "stats_players` WHERE `uuid` LIKE '"
					+ playerPoints.getUUID().toString() + "';";

			int oldKills = 0, oldAxeKills = 0, oldDirectArrowKills = 0, oldExplosionKills = 0, oldKnifeKills = 0,
					oldZombieKills = 0, oldDeaths = 0, oldAxeDeaths = 0, oldDirectArrowDeaths = 0,
					oldExplosionDeaths = 0, oldKnifeDeaths = 0, oldWins = 0, oldScore = 0, oldGames = 0;

			RMConnection conn = connect.getConnection();
			Statement statement = null;
			try {
				statement = conn.createStatement();
				ResultSet rs = conn.executeQuery(statement, query);
				if (rs.next()) {
					oldKills = rs.getInt("kills");
					oldAxeKills = rs.getInt("axe_kills");
					oldDirectArrowKills = rs.getInt("direct_arrow_kills");
					oldExplosionKills = rs.getInt("explosion_kills");
					oldKnifeKills = rs.getInt("knife_kills");
					oldZombieKills = rs.getInt("zombie_kills");

					oldDeaths = rs.getInt("deaths");
					oldAxeDeaths = rs.getInt("axe_deaths");
					oldDirectArrowDeaths = rs.getInt("direct_arrow_deaths");
					oldExplosionDeaths = rs.getInt("explosion_deaths");
					oldKnifeDeaths = rs.getInt("knife_deaths");

					oldWins = rs.getInt("wins");
					oldScore = rs.getInt("score");
					oldGames = rs.getInt("games");
				}

				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			} finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}

			int newKills = oldKills + playerPoints.getKills(), newAxeKills = oldAxeKills + playerPoints.getAxeKills(),
					newDirectArrowKills = oldDirectArrowKills + playerPoints.getDirectArrowKills(),
					newExplosionKills = oldExplosionKills + playerPoints.getExplosionKills(),
					newKnifeKills = oldKnifeKills + playerPoints.getKnifeKills(),
					newZombieKills = oldZombieKills + playerPoints.getZombieKills(),

					newDeaths = oldDeaths + playerPoints.getDeaths(),
					newAxeDeaths = oldAxeDeaths + playerPoints.getAxeDeaths(),
					newDirectArrowDeaths = oldDirectArrowDeaths + playerPoints.getDirectArrowDeaths(),
					newExplosionDeaths = oldExplosionDeaths + playerPoints.getExplosionDeaths(),
					newKnifeDeaths = oldKnifeDeaths + playerPoints.getKnifeDeaths(),

					newWins = (playerPoints.isWinner()) ? oldWins + 1 : oldWins,
					newScore = oldScore + playerPoints.getPoints(), newGames = oldGames + 1;

			double newKD = (newDeaths != 0) ? (((double) newKills) / ((double) newDeaths)) : 1;

			PreparedStatement prestt = null;
			try {
				prestt = conn.prepareStatement("REPLACE INTO `" + connect.getPrefix()
						+ "stats_players` (name, uuid, kills, axe_kills, direct_arrow_kills, explosion_kills,"
						+ " knife_kills, zombie_kills, deaths, axe_deaths, direct_arrow_deaths, explosion_deaths,"
						+ " knife_deaths, wins, score, games, kd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
				if (prestt == null) {
					return null;
				}

				prestt.setString(1, Bukkit.getOfflinePlayer(playerPoints.getUUID()).getName());
				prestt.setString(2, playerPoints.getUUID().toString());
				prestt.setInt(3, newKills);
				prestt.setInt(4, newAxeKills);
				prestt.setInt(5, newDirectArrowKills);
				prestt.setInt(6, newExplosionKills);
				prestt.setInt(7, newKnifeKills);
				prestt.setInt(8, newZombieKills);
				prestt.setInt(9, newDeaths);
				prestt.setInt(10, newAxeDeaths);
				prestt.setInt(11, newDirectArrowDeaths);
				prestt.setInt(12, newExplosionDeaths);
				prestt.setInt(13, newKnifeDeaths);
				prestt.setInt(14, newWins);
				prestt.setInt(15, newScore);
				prestt.setInt(16, newGames);
				prestt.setDouble(17, newKD);
				prestt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			} finally {
				if (prestt != null) {
					try {
						prestt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
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
			int oldPoints = 0;
			String query = "SELECT * FROM `" + connect.getPrefix() + "stats_players` WHERE uuid LIKE '"
					+ uuid.toString() + "';";
			RMConnection conn = connect.getConnection();
			Statement statement = null;
			try {
				statement = conn.createStatement();
				ResultSet rs = conn.executeQuery(statement, query);
				if (rs.next()) {
					oldPoints = rs.getInt("score");
				}

				rs.close();
			} catch (SQLException e) {
			} finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}

			PreparedStatement prestt = null;
			try {
				prestt = conn.prepareStatement("REPLACE INTO `" + connect.getPrefix()
						+ "stats_players` (name, uuid, score) VALUES (?, ?, ?);");
				if (prestt == null) {
					return null;
				}

				prestt.setString(1, Bukkit.getOfflinePlayer(uuid).getName());
				prestt.setString(2, uuid.toString());
				prestt.setInt(3, oldPoints + points);
				prestt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (prestt != null) {
					try {
						prestt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}

			return null;
		});
	}

	@Override
	public List<PlayerPoints> getAllPlayerStatistics() {
		if (!connect.isValid()) {
			return Collections.emptyList();
		}

		return connect.dispatchAsync(() -> {
			final List<PlayerPoints> allRPPs = new ArrayList<>();

			RMConnection conn = connect.getConnection();
			Statement statement = null;
			String query = "SELECT * FROM `" + connect.getPrefix() + "stats_players`;";
			try {
				statement = conn.createStatement();
				ResultSet rs = conn.executeQuery(statement, query);
				while (rs.next()) {
					String uuid = rs.getString("uuid");
					if (uuid != null) {
						UUID uuid2 = UUID.fromString(uuid);
						PlayerPoints pp = RuntimePPManager.getPPForPlayer(uuid2);
						if (pp == null) {
							pp = new PlayerPoints(uuid2);
						}

						allRPPs.add(pp);
					}
				}

				rs.close();
			} catch (SQLException e) {
			} finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}

			return allRPPs;
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

			RMConnection conn = connect.getConnection();

			Statement statement = null;
			String query = "SELECT * FROM `" + connect.getPrefix() + "stats_players` WHERE `uuid` LIKE '"
					+ uuid.toString() + "';";

			int currentKills = 0, currentAxeKills = 0, currentDirectArrowKills = 0, currentExplosionKills = 0,
					currentKnifeKills = 0, currentZombieKills = 0, currentDeaths = 0, currentAxeDeaths = 0,
					currentDirectArrowDeaths = 0, currentExplosionDeaths = 0, currentKnifeDeaths = 0, currentWins = 0,
					currentScore = 0, currentGames = 0;

			double currentKD = 0;

			try {
				statement = conn.createStatement();
				ResultSet rs = conn.executeQuery(statement, query);
				if (rs.next()) {
					currentKills = rs.getInt("kills");
					currentAxeKills = rs.getInt("axe_kills");
					currentDirectArrowKills = rs.getInt("direct_arrow_kills");
					currentExplosionKills = rs.getInt("explosion_kills");
					currentKnifeKills = rs.getInt("knife_kills");
					currentZombieKills = rs.getInt("zombie_kills");

					currentDeaths = rs.getInt("deaths");
					currentAxeDeaths = rs.getInt("axe_deaths");
					currentDirectArrowDeaths = rs.getInt("direct_arrow_deaths");
					currentExplosionDeaths = rs.getInt("explosion_deaths");
					currentKnifeDeaths = rs.getInt("knife_deaths");

					currentWins = rs.getInt("wins");
					currentScore = rs.getInt("score");
					currentGames = rs.getInt("games");
					currentKD = rs.getDouble("kd");

					pp.setKills(currentKills);
					pp.setAxeKills(currentAxeKills);
					pp.setDirectArrowKills(currentDirectArrowKills);
					pp.setExplosionKills(currentExplosionKills);
					pp.setKnifeKills(currentKnifeKills);
					pp.setZombieKills(currentZombieKills);

					pp.setDeaths(currentDeaths);
					pp.setAxeDeaths(currentAxeDeaths);
					pp.setDirectArrowDeaths(currentDirectArrowDeaths);
					pp.setExplosionDeaths(currentExplosionDeaths);
					pp.setKnifeDeaths(currentKnifeDeaths);

					pp.setWins(currentWins);
					pp.setPoints(currentScore);
					pp.setGames(currentGames);
					pp.setKD(currentKD);
				}

				rs.close();
			} catch (SQLException e) {
				return null;
			} finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
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

		final RMConnection conn = connect.getConnection();
		if (!conn.isConnected()) {
			return false;
		}

		return connect.dispatchAsync(() -> {
			PreparedStatement prestt = null;
			try {
				prestt = conn.prepareStatement("REPLACE INTO `" + connect.getPrefix()
						+ "stats_players` (name, uuid, kills, axe_kills, direct_arrow_kills, explosion_kills,"
						+ " knife_kills, zombie_kills, deaths, axe_deaths, direct_arrow_deaths, explosion_deaths,"
						+ " knife_deaths, wins, score, games, kd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
				if (prestt == null) {
					return false;
				}

				prestt.setString(1, Bukkit.getOfflinePlayer(rpp.getUUID()).getName());
				prestt.setString(2, rpp.getUUID().toString());
				for (int i = 3; i <= 16; i++) {
					prestt.setInt(i, 0);
				}
				prestt.setDouble(17, 0d);
				prestt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (prestt != null) {
					try {
						prestt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}

			return true;
		}).join();
	}

	@Override
	public void loadJoinDelay() {
		if (!ConfigValues.isRejoinDelayEnabled() || !ConfigValues.isRememberRejoinDelay() || !connect.isConnected()) {
			return;
		}

		connect.dispatchAsync(() -> {
			String query = "SELECT * FROM `" + connect.getPrefix() + "players`;";
			RMConnection conn = connect.getConnection();
			Statement statement = null;
			try {
				statement = conn.createStatement();
				ResultSet rs = conn.executeQuery(statement, query);
				PreparedStatement ps = null;
				while (rs.next()) {
					OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("uuid")));
					if (p != null) {
						ReJoinDelay.setTime(p, rs.getLong("time"));
					}

					int id = rs.getInt("id");
					String s = "DELETE FROM `" + connect.getPrefix() + "players` WHERE id = ?;";
					ps = conn.prepareStatement(s);
					ps.setInt(1, id);
					ps.executeUpdate();
				}

				if (ps != null) {
					ps.close();
				}
				rs.close();
			} catch (SQLException e) {
			} finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}

			return null;
		});
	}

	@Override
	public void saveJoinDelay() {
		if (!ConfigValues.isRejoinDelayEnabled() || !ConfigValues.isRememberRejoinDelay() || !connect.isConnected()) {
			return;
		}

		connect.dispatchAsync(() -> {
			PreparedStatement prestt = null;
			try {
				prestt = connect.getConnection().prepareStatement(
						"REPLACE INTO `" + connect.getPrefix() + "players` (uuid, time) VALUES (?, ?);");
				if (prestt == null) {
					return null;
				}

				for (Map.Entry<OfflinePlayer, Long> m : ReJoinDelay.getPlayerTimes().entrySet()) {
					prestt.setString(1, m.getKey().getUniqueId().toString());
					prestt.setLong(2, m.getValue());
					prestt.executeUpdate();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (prestt != null) {
					try {
						prestt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}

			return null;
		});
	}

	@Override
	public void connectDatabase() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			Debug.logConsole(Level.WARNING, "Could not connect to the SQL database. No Sql found.");
			return;
		}

		String prefix = ConfigValues.getDatabaseTablePrefix();
		if (prefix.isEmpty()) {
			prefix = "rm_";
		}

		File sqlFile = new File(RageMode.getInstance().getFolder(), ConfigValues.getSqlFileName() + ".db");
		if (!sqlFile.exists()) {
			try {
				sqlFile.createNewFile();
			} catch (java.io.IOException e) {
				e.printStackTrace();
			}
		}

		if ((connect = new SQLConnect(sqlFile, prefix)).isConnected()) {
			Debug.logConsole("Successfully connected to SQL!");
		}
	}

	@Override
	public void loadDatabase(boolean startup) {
		if (connect == null || !connect.isConnected()) {
			connectDatabase();
		}

		RuntimePPManager.loadPPListFromDatabase();
		loadPlayerStatistics();

		if (startup) {
			loadJoinDelay();
		}
	}

	@Override
	public void saveDatabase() {
		saveData();
		saveJoinDelay();
	}

	@Override
	public boolean convertDatabase(final String type) {
		if (type == null || type.trim().isEmpty()
				|| RageMode.getInstance().getDatabaseType().toString().equalsIgnoreCase(type.trim())) {
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

		final Configuration conf = RageMode.getInstance().getConfiguration();
		conf.getCfg().set("database.type", type);
		Configuration.saveFile(conf.getCfg(), conf.getCfgFile());

		RageMode.getInstance().setDatabase(type);

		RuntimePPManager.getRuntimePPList().forEach(RageMode.getInstance().getDatabase()::addPlayerStatistics);
		RuntimePPManager.loadPPListFromDatabase();
		return true;
	}
}