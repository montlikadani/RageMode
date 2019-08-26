package hu.montlikadani.ragemode.statistics;

import java.sql.Connection;
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
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;

public class MySQLStats {

	private static List<RetPlayerPoints> points = new ArrayList<>();

	public static void loadPlayerStatistics(MySQLConnect mySQL) {
		if (mySQL.getConnection() == null || !mySQL.getConnection().isConnected()) {
			return;
		}

		int totalPlayers = 0;

		points.clear();

		Connection connection = mySQL.getConnection().getConnection();

		Statement statement = null;
		String query = "SELECT * FROM " + mySQL.getPrefix() + "stats_players;";

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
			statement = connection.createStatement();
			ResultSet rs = mySQL.getConnection().executeQuery(statement, query);
			while (rs.next()) {
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

				String playerUUID = rs.getString("uuid");
				RetPlayerPoints rPP = RuntimeRPPManager.getRPPForPlayer(playerUUID);
				if (rPP == null) {
					rPP = new RetPlayerPoints(playerUUID);
					RageScores.getPlayerPointsMap().put(UUID.fromString(playerUUID).toString(), rPP);
				}

				rPP.setKills(currentKills);
				rPP.setAxeKills(currentAxeKills);
				rPP.setDirectArrowKills(currentDirectArrowKills);
				rPP.setExplosionKills(currentExplosionKills);
				rPP.setKnifeKills(currentKnifeKills);

				rPP.setDeaths(currentDeaths);
				rPP.setAxeDeaths(currentAxeDeaths);
				rPP.setDirectArrowDeaths(currentDirectArrowDeaths);
				rPP.setExplosionDeaths(currentExplosionDeaths);
				rPP.setKnifeDeaths(currentKnifeDeaths);

				rPP.setWins(currentWins);
				rPP.setPoints(currentScore);
				rPP.setGames(currentGames);
				rPP.setKD(currentKD);

				points.add(rPP);

				++totalPlayers;
			}
			rs.close();
		} catch (SQLException e) {
			try {
				connection.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			return;
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		if (totalPlayers > 0)
			Debug.logConsole("Loaded " + totalPlayers + " player" + (totalPlayers > 1 ? "s" : "") + " database.");
	}

	/**
	 * {@link #addPlayerStatistics(PlayerPoints, MySQLConnect)}
	 * @param playerPoints PlayerPoints
	 */
	public static void addPlayerStatistics(PlayerPoints playerPoints) {
		addPlayerStatistics(playerPoints, RageMode.getMySQL());
	}

	/**
	 * Adds the statistics from the given PlayerPoints instance to the database
	 * connection from the given MySQLConnect instance.
	 * 
	 * @param playerPoints The PlayerPoints instance from which the statistics should be gotten.
	 * @param mySQL The MySQLConnect instance which holds the Connection for the database.
	 */
	public static void addPlayerStatistics(PlayerPoints playerPoints, MySQLConnect mySQL) {
		if (mySQL.getConnection() == null || !mySQL.getConnection().isValid())
			return;

		Connection connection = mySQL.getConnection().getConnection();

		Statement statement = null;
		String query = "SELECT * FROM " + mySQL.getPrefix() + "stats_players WHERE uuid LIKE '" + playerPoints.getPlayerUUID() + "';";

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

		try {
			statement = connection.createStatement();
			ResultSet rs = mySQL.getConnection().executeQuery(statement, query);
			while (rs.next()) {
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
			Debug.logConsole(Bukkit.getPlayer(UUID.fromString(playerPoints.getPlayerUUID()))
					+ " has no statistics yet! Creating one special row for him...");
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
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

		query = "REPLACE INTO " + mySQL.getPrefix() + "stats_players (name, uuid, kills, axe_kills, direct_arrow_kills, explosion_kills, knife_kills, deaths, axe_deaths, direct_arrow_deaths, explosion_deaths, knife_deaths, wins, score, games, kd) VALUES ("
				+ "'" + Bukkit.getPlayer(UUID.fromString(playerPoints.getPlayerUUID())).getName() + "', " + "'"
				+ playerPoints.getPlayerUUID() + "', " + Integer.toString(newKills) + ", "
				+ Integer.toString(newAxeKills) + ", " + Integer.toString(newDirectArrowKills) + ", "
				+ Integer.toString(newExplosionKills) + ", " + Integer.toString(newKnifeKills) + ", "
				+ Integer.toString(newDeaths) + ", " + Integer.toString(newAxeDeaths) + ", "
				+ Integer.toString(newDirectArrowDeaths) + ", " + Integer.toString(newExplosionDeaths) + ", "
				+ Integer.toString(newKnifeDeaths) + ", " + Integer.toString(newWins) + ", "
				+ Integer.toString(newScore) + ", " + Integer.toString(newGames) + ", " + Double.toString(newKD) + ");";
		try {
			mySQL.getConnection().executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns an RetPlayerPoints instance with the statistics from the database
	 * connection for the given Player.
	 * 
	 * @param playerUUID The Player instance for which the statistic should be gotten.
	 * @return {@link RetPlayerPoints} class
	 */
	public static RetPlayerPoints getPlayerStatistics(String playerUUID) {
		if (!points.isEmpty()) {
			for (RetPlayerPoints rpp : points) {
				if (rpp.getPlayerUUID().equals(playerUUID)) {
					return rpp;
				}
			}
		}

		return null;
	}

	/**
	 * Retrieves the list of RetPlayerPoints.
	 * @return A List of all RetPlayerPoints objects which are stored in the MySQL database.
	 */
	public static List<RetPlayerPoints> getAllPlayerStatistics() {
		MySQLConnect connect = RageMode.getMySQL();
		if (connect.getConnection() == null || !connect.getConnection().isValid())
			return Collections.emptyList();

		String uuid = null;
		List<RetPlayerPoints> allRPPs = new ArrayList<>();

		Connection connection = connect.getConnection().getConnection();
		Statement statement = null;
		String query = "SELECT * FROM " + connect.getPrefix() + "stats_players";
		try {
			statement = connection.createStatement();
			ResultSet rs = connect.getConnection().executeQuery(statement, query);
			while (rs.next()) {
				uuid = rs.getString("uuid");
			}
			rs.close();
		} catch (SQLException e) {
			try {
				connection.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return Collections.emptyList();
		} finally {
			try {
				if (statement != null)
					statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (uuid != null) {
			allRPPs.add(getPlayerStatistics(uuid));
		}

		return allRPPs;
	}

	/**
	 * Restores all data of the specified player to 0
	 * @param uuid UUID of player
	 * @return true if the player found in database
	 */
	public static boolean resetPlayerStatistic(String uuid) {
		if (RageMode.getMySQL().getConnection() == null || !RageMode.getMySQL().getConnection().isConnected())
			return false;

		RetPlayerPoints rpp = RuntimeRPPManager.getRPPForPlayer(uuid);
		if (rpp == null)
			return false;

		Connection connection = RageMode.getMySQL().getConnection().getConnection();

		Statement statement = null;
		String query = "SELECT * FROM " + RageMode.getMySQL().getPrefix() + "stats_players;";

		try {
			statement = connection.createStatement();
			ResultSet rs = RageMode.getMySQL().getConnection().executeQuery(statement, query);
			while (rs.next()) {
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
			}
			rs.close();
		} catch (SQLException e) {
			try {
				connection.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			return false;
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		query = "REPLACE INTO " + RageMode.getMySQL().getPrefix() + "stats_players (name, uuid, kills, axe_kills, direct_arrow_kills, explosion_kills, knife_kills, deaths, axe_deaths, direct_arrow_deaths, explosion_deaths, knife_deaths, wins, score, games, kd) VALUES ("
				+ "'" + Bukkit.getPlayer(UUID.fromString(rpp.getPlayerUUID())).getName() + "', " + "'"
				+ rpp.getPlayerUUID() + "', " + Integer.toString(0) + ", "
				+ Integer.toString(0) + ", " + Integer.toString(0) + ", "
				+ Integer.toString(0) + ", " + Integer.toString(0) + ", "
				+ Integer.toString(0) + ", " + Integer.toString(0) + ", "
				+ Integer.toString(0) + ", " + Integer.toString(0) + ", "
				+ Integer.toString(0) + ", " + Integer.toString(0) + ", "
				+ Integer.toString(0) + ", " + Integer.toString(0) + ", " + Double.toString(0d) + ");";
		try {
			RageMode.getMySQL().getConnection().executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
}