package hu.montlikadani.ragemode.statistics;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.database.MySQLConnect;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;

public class MySQLStats {

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
	 * @param mySQLConnect The MySQLConnect instance which holds the Connection for the database.
	 */
	public static void addPlayerStatistics(PlayerPoints playerPoints, MySQLConnect mySQLConnect) {
		if (!mySQLConnect.isValid())
			return;

		Connection connection = mySQLConnect.getConnection();

		Statement statement = null;
		String query = "SELECT * FROM " + mySQLConnect.getPrefix() + "stats_players WHERE uuid LIKE '" + playerPoints.getPlayerUUID() + "';";

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
			ResultSet rs = statement.executeQuery(query);
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
			RageMode.logConsole("[RageMode] " + Bukkit.getPlayer(UUID.fromString(playerPoints.getPlayerUUID()))
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

		statement = null;
		query = "REPLACE INTO " + mySQLConnect.getPrefix() + "stats_players (name, uuid, kills, axe_kills, direct_arrow_kills, explosion_kills, knife_kills, deaths, axe_deaths, direct_arrow_deaths, explosion_deaths, knife_deaths, wins, score, games, kd) VALUES ("
				+ "'" + Bukkit.getPlayer(UUID.fromString(playerPoints.getPlayerUUID())).getName() + "', " + "'"
				+ playerPoints.getPlayerUUID() + "', " + Integer.toString(newKills) + ", "
				+ Integer.toString(newAxeKills) + ", " + Integer.toString(newDirectArrowKills) + ", "
				+ Integer.toString(newExplosionKills) + ", " + Integer.toString(newKnifeKills) + ", "
				+ Integer.toString(newDeaths) + ", " + Integer.toString(newAxeDeaths) + ", "
				+ Integer.toString(newDirectArrowDeaths) + ", " + Integer.toString(newExplosionDeaths) + ", "
				+ Integer.toString(newKnifeDeaths) + ", " + Integer.toString(newWins) + ", "
				+ Integer.toString(newScore) + ", " + Integer.toString(newGames) + ", " + Double.toString(newKD) + ");";
		try {
			statement = connection.createStatement();
			statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException s) {
				s.printStackTrace();
			}
		}
	}

	/**
	 * Gets the {@link #getPlayerStatistics(String, MySQLConnect)}
	 * @param playerUUID Player uuid
	 */
	public static RetPlayerPoints getPlayerStatistics(String playerUUID) {
		return getPlayerStatistics(playerUUID, RageMode.getMySQL());
	}

	/**
	 * Returns an RetPlayerPoints instance with the statistics from the database
	 * connection for the given Player.
	 * 
	 * @param player The Player instance for which the statistic should be gotten.
	 * @param mySQLConnect The MySQLConnect which holds the Connection instance for the database.
	 * @return
	 */
	public static RetPlayerPoints getPlayerStatistics(String playerUUID, MySQLConnect mySQLConnector) {
		if (!mySQLConnector.isValid())
			return null;

		Connection connection = mySQLConnector.getConnection();

		Statement statement = null;
		String query = "SELECT * FROM " + mySQLConnector.getPrefix() + "stats_players WHERE uuid LIKE '" + playerUUID + "';";

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
			ResultSet rs = statement.executeQuery(query);
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
			}
			rs.close();
		} catch (SQLException e) {
			return null;
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (currentGames == 0)
			return null;

		RetPlayerPoints retPlayerPoints = new RetPlayerPoints(playerUUID);
		retPlayerPoints.setKills(currentKills);
		retPlayerPoints.setAxeKills(currentAxeKills);
		retPlayerPoints.setDirectArrowKills(currentDirectArrowKills);
		retPlayerPoints.setExplosionKills(currentExplosionKills);
		retPlayerPoints.setKnifeKills(currentKnifeKills);

		retPlayerPoints.setDeaths(currentDeaths);
		retPlayerPoints.setAxeDeaths(currentAxeDeaths);
		retPlayerPoints.setDirectArrowDeaths(currentDirectArrowDeaths);
		retPlayerPoints.setExplosionDeaths(currentExplosionDeaths);
		retPlayerPoints.setKnifeDeaths(currentKnifeDeaths);

		retPlayerPoints.setWins(currentWins);
		retPlayerPoints.setPoints(currentScore);
		retPlayerPoints.setGames(currentGames);
		retPlayerPoints.setKD(currentKD);

		return retPlayerPoints;
	}

	/**
	 * Retrieves all rows from the mySQL database and returns them as a List of RetPlayerPoints.
	 * 
	 * @return A List of all RetPlayerPoints objects which are stored in the mySQL database.
	 */
	public static List<RetPlayerPoints> getAllPlayerStatistics() {
		List<RetPlayerPoints> rppList = new java.util.ArrayList<>();

		if (!RageMode.getMySQL().isConnected())
			return Collections.emptyList();

		Connection connection = RageMode.getMySQL().getConnection();

		Statement statement = null;
		String query = "SELECT * FROM " + RageMode.getMySQL().getPrefix() + "stats_players;";

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
			ResultSet rs = statement.executeQuery(query);
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
				RetPlayerPoints retPlayerPoints = new RetPlayerPoints(playerUUID);
				retPlayerPoints.setKills(currentKills);
				retPlayerPoints.setAxeKills(currentAxeKills);
				retPlayerPoints.setDirectArrowKills(currentDirectArrowKills);
				retPlayerPoints.setExplosionKills(currentExplosionKills);
				retPlayerPoints.setKnifeKills(currentKnifeKills);

				retPlayerPoints.setDeaths(currentDeaths);
				retPlayerPoints.setAxeDeaths(currentAxeDeaths);
				retPlayerPoints.setDirectArrowDeaths(currentDirectArrowDeaths);
				retPlayerPoints.setExplosionDeaths(currentExplosionDeaths);
				retPlayerPoints.setKnifeDeaths(currentKnifeDeaths);

				retPlayerPoints.setWins(currentWins);
				retPlayerPoints.setPoints(currentScore);
				retPlayerPoints.setGames(currentGames);
				retPlayerPoints.setKD(currentKD);

				rppList.add(retPlayerPoints);
			}
			rs.close();
		} catch (SQLException e) {
			return null;
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rppList;
	}

	/**
	 * Restores all data of the specified player to 0
	 * 
	 * @param uuid UUID of player
	 * @return true if the player found in database
	 */
	public static boolean resetPlayerStatistic(String uuid) {
		if (!RageMode.getMySQL().isConnected())
			return false;

		RetPlayerPoints rpp = RuntimeRPPManager.getRPPForPlayer(uuid);
		if (rpp == null)
			return false;

		Connection connection = RageMode.getMySQL().getConnection();

		Statement statement = null;
		String query = "SELECT * FROM " + RageMode.getMySQL().getPrefix() + "stats_players;";

		try {
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);
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
			return false;
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		statement = null;
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
			statement = connection.createStatement();
			statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
}