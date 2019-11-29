package hu.montlikadani.ragemode.statistics;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.database.MySQLConnect;
import hu.montlikadani.ragemode.database.RMConnection;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;

public class MySQLStats {

	private static List<PlayerPoints> points = new ArrayList<>();

	public static void loadPlayerStatistics() {
		MySQLConnect mySQL = RageMode.getMySQL();
		if (!mySQL.isConnected()) {
			return;
		}

		if (!mySQL.isValid()) {
			return;
		}

		int totalPlayers = 0;

		points.clear();

		int kills = 0;
		int axeKills = 0;
		int directArrowKills = 0;
		int explosionKills = 0;
		int knifeKills = 0;

		int deaths = 0;
		int axeDeaths = 0;
		int directArrowDeaths = 0;
		int explosionDeaths = 0;
		int knifeDeaths = 0;

		double kd = 0d;

		int currentWins = 0;
		int currentScore = 0;
		int currentGames = 0;

		String query = "SELECT * FROM `" + mySQL.getPrefix() + "stats_players`;";
		RMConnection conn = mySQL.getConnection();
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

				rPP.setKD(kd);

				rPP.setWins(currentWins);
				rPP.setPoints(currentScore);
				rPP.setGames(currentGames);

				points.add(rPP);

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

		if (totalPlayers > 0)
			Debug.logConsole("Loaded {0} player{1} stats database.", totalPlayers, (totalPlayers > 1 ? "s" : ""));
	}

	/**
	 * Adds the statistics from the given PlayerPoints instance to the database
	 * connection from the MySQLConnect instance.
	 * 
	 * @param playerPoints The PlayerPoints instance from which the statistics should be gotten.
	 */
	public static void addPlayerStatistics(PlayerPoints playerPoints) {
		MySQLConnect mySQL = RageMode.getMySQL();
		if (!mySQL.isValid())
			return;

		String query = "SELECT * FROM `" + mySQL.getPrefix() + "stats_players` WHERE uuid LIKE `"
				+ playerPoints.getUUID() + "`;";

		int oldKills = 0;
		int oldAxeKills = 0;
		int oldDirectArrowKills = 0;
		int oldExplosionKills = 0;
		int oldKnifeKills = 0;

		int oldDeaths = 0;
		int oldAxeDeaths = 0;
		int oldDirectArrowDeaths = 0;
		int oldExplosionDeaths = 0;
		int oldKnifeDeaths = 0;

		int oldWins = 0;
		int oldScore = 0;
		int oldGames = 0;

		RMConnection conn = mySQL.getConnection();
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
			Debug.logConsole(Bukkit.getPlayer(playerPoints.getUUID()).getName()
					+ " has no statistics yet! Creating one special row for him...");
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		int newKills = oldKills + playerPoints.getKills();
		int newAxeKills = oldAxeKills + playerPoints.getAxeKills();
		int newDirectArrowKills = oldDirectArrowKills + playerPoints.getDirectArrowKills();
		int newExplosionKills = oldExplosionKills + playerPoints.getExplosionKills();
		int newKnifeKills = oldKnifeKills + playerPoints.getKnifeKills();

		int newDeaths = oldDeaths + playerPoints.getDeaths();
		int newAxeDeaths = oldAxeDeaths + playerPoints.getAxeDeaths();
		int newDirectArrowDeaths = oldDirectArrowDeaths + playerPoints.getDirectArrowDeaths();
		int newExplosionDeaths = oldExplosionDeaths + playerPoints.getExplosionDeaths();
		int newKnifeDeaths = oldKnifeDeaths + playerPoints.getKnifeDeaths();

		int newWins = (playerPoints.isWinner()) ? oldWins + 1 : oldWins;
		int newScore = oldScore + playerPoints.getPoints();
		int newGames = oldGames + 1;
		double newKD = (newDeaths != 0) ? (((double) newKills) / ((double) newDeaths)) : 1;

		PreparedStatement prestt = null;
		try {
			prestt = conn.prepareStatement("REPLACE INTO `" + mySQL.getPrefix()
					+ "stats_players` (name, uuid, kills, axe_kills, direct_arrow_kills, explosion_kills,"
					+ " knife_kills, deaths, axe_deaths, direct_arrow_deaths, explosion_deaths,"
					+ " knife_deaths, wins, score, games, kd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			if (prestt == null) {
				return;
			}

			prestt.setString(1, Bukkit.getPlayer(playerPoints.getUUID()).getName());
			prestt.setString(2, playerPoints.getUUID().toString());
			prestt.setInt(3, newKills);
			prestt.setInt(4, newAxeKills);
			prestt.setInt(5, newDirectArrowKills);
			prestt.setInt(6, newExplosionKills);
			prestt.setInt(7, newKnifeKills);
			prestt.setInt(8, newDeaths);
			prestt.setInt(9, newAxeDeaths);
			prestt.setInt(10, newDirectArrowDeaths);
			prestt.setInt(11, newExplosionDeaths);
			prestt.setInt(12, newKnifeDeaths);
			prestt.setInt(13, newWins);
			prestt.setInt(14, newScore);
			prestt.setInt(15, newGames);
			prestt.setDouble(16, newKD);
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
	}

	/**
	 * Add points to the given player database.
	 * @param points the amount of points
	 * @param uuid player uuid
	 */
	public static void addPoints(int points, UUID uuid) {
		MySQLConnect mysql = RageMode.getMySQL();
		if (!mysql.isValid())
			return;

		int oldPoints = 0;
		String query = "SELECT * FROM `" + mysql.getPrefix() + "stats_players` WHERE uuid LIKE `" + uuid + "`;";
		RMConnection conn = mysql.getConnection();
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
			prestt = conn.prepareStatement(
					"REPLACE INTO `" + mysql.getPrefix() + "stats_players` (name, uuid, score) VALUES (?, ?, ?);");
			if (prestt == null) {
				return;
			}

			prestt.setString(1, Bukkit.getPlayer(uuid).getName());
			prestt.setString(2, uuid.toString());
			prestt.setInt(14, oldPoints + points);
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
	}

	/**
	 * Returns an PlayerPoints instance with the statistics from the database
	 * connection for the given Player.
	 * 
	 * @param playerUUID The Player instance for which the statistic should be gotten.
	 * @return {@link PlayerPoints} class
	 */
	@Deprecated
	public static PlayerPoints getPlayerStatistics(String playerUUID) {
		return getPlayerStatistics(UUID.fromString(playerUUID));
	}

	/**
	 * Returns an PlayerPoints instance with the statistics from the database
	 * connection for the given Player.
	 * 
	 * @param playerUUID The Player instance for which the statistic should be gotten.
	 * @return {@link PlayerPoints} class
	 */
	public static PlayerPoints getPlayerStatistics(UUID uuid) {
		for (PlayerPoints rpp : points) {
			if (rpp.getUUID().equals(uuid)) {
				return rpp;
			}
		}

		return null;
	}

	/**
	 * Retrieves the list of PlayerPoints.
	 * @return A List of all PlayerPoints objects which are stored in the MySQL database.
	 */
	public static List<PlayerPoints> getAllPlayerStatistics() {
		MySQLConnect connect = RageMode.getMySQL();
		if (!connect.isValid())
			return Collections.emptyList();

		List<PlayerPoints> allRPPs = new ArrayList<>();

		RMConnection conn = connect.getConnection();
		Statement statement = null;
		String query = "SELECT * FROM `" + connect.getPrefix() + "stats_players`;";
		try {
			statement = conn.createStatement();
			ResultSet rs = conn.executeQuery(statement, query);
			while (rs.next()) {
				String uuid = rs.getString("uuid");
				if (uuid != null && getPlayerStatistics(uuid) != null) {
					allRPPs.add(getPlayerStatistics(uuid));
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
	}

	/**
	 * Gets all player stats from database.
	 * <p>If the player never played and not found in the database, will be ignored.
	 * 
	 * @param uuid Player uuid
	 * @return {@link PlayerPoints}
	 */
	@Deprecated
	public static PlayerPoints getPlayerStatsFromData(String uuid) {
		return getPlayerStatsFromData(UUID.fromString(uuid));
	}

	/**
	 * Gets all player stats from database.
	 * <p>If the player never played and not found in the database, will be ignored.
	 * 
	 * @param uuid Player uuid
	 * @return {@link PlayerPoints}
	 */
	public static PlayerPoints getPlayerStatsFromData(UUID uuid) {
		MySQLConnect connect = RageMode.getMySQL();
		if (!connect.isValid())
			return null;

		PlayerPoints pp = RuntimePPManager.getPPForPlayer(uuid);
		if (pp == null) {
			return null;
		}

		// Cloning to ignore overwrite
		pp = (PlayerPoints) pp.clone();

		RMConnection conn = connect.getConnection();

		Statement statement = null;
		String query = "SELECT * FROM `" + connect.getPrefix() + "stats_players` WHERE uuid LIKE `" + uuid + "`;";

		int currentKills = 0;
		int currentAxeKills = 0;
		int currentDirectArrowKills = 0;
		int currentExplosionKills = 0;
		int currentKnifeKills = 0;

		int currentDeaths = 0;
		int currentAxeDeaths = 0;
		int currentDirectArrowDeaths = 0;
		int currentExplosionDeaths = 0;
		int currentKnifeDeaths = 0;

		int currentWins = 0;
		int currentScore = 0;
		int currentGames = 0;
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
	}

	/**
	 * Restores all data of the given player to 0
	 * @param uuid UUID of player
	 * @return true if the player found in database
	 */
	@Deprecated
	public static boolean resetPlayerStatistic(String uuid) {
		return resetPlayerStatistic(UUID.fromString(uuid));
	}

	/**
	 * Restores all data of the given player to 0
	 * @param uuid UUID of player
	 * @return true if the player found in database
	 */
	public static boolean resetPlayerStatistic(UUID uuid) {
		PlayerPoints rpp = RuntimePPManager.getPPForPlayer(uuid);
		if (rpp == null)
			return false;

		rpp.setKills(0);
		rpp.setAxeKills(0);
		rpp.setDirectArrowKills(0);
		rpp.setExplosionKills(0);
		rpp.setKnifeKills(0);

		rpp.setDeaths(0);
		rpp.setAxeDeaths(0);
		rpp.setDirectArrowDeaths(0);
		rpp.setExplosionDeaths(0);
		rpp.setKnifeDeaths(0);

		rpp.setWins(0);
		rpp.setPoints(0);
		rpp.setGames(0);
		rpp.setKD(0d);

		RMConnection conn = RageMode.getMySQL().getConnection();
		if (!conn.isConnected()) {
			return false;
		}

		PreparedStatement prestt = null;
		try {
			prestt = conn.prepareStatement("REPLACE INTO `" + RageMode.getMySQL().getPrefix()
					+ "stats_players` (name, uuid, kills, axe_kills, direct_arrow_kills, explosion_kills,"
					+ " knife_kills, deaths, axe_deaths, direct_arrow_deaths, explosion_deaths,"
					+ " knife_deaths, wins, score, games, kd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			if (prestt == null) {
				return false;
			}

			prestt.setString(1, Bukkit.getPlayer(rpp.getUUID()).getName());
			prestt.setString(2, rpp.getUUID().toString());
			for (int i = 3; i <= 15; i++) {
				prestt.setInt(i, 0);
			}
			prestt.setDouble(16, 0d);
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
	}
}