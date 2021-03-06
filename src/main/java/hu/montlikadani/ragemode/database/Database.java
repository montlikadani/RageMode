package hu.montlikadani.ragemode.database;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import hu.montlikadani.ragemode.database.connector.DBConnector;
import hu.montlikadani.ragemode.scores.PlayerPoints;

/**
 * Database interface which represents the required methods to load/save
 * data(s).
 */
public interface Database {

	/**
	 * @return the current database type of this inherited class. If annotation is
	 *         not present returns {@link DBType#YAML} as default
	 */
	@NotNull
	default DBType getDatabaseType() {
		return DBType.YAML;
	}

	/**
	 * Returns the current connected database. In some situations this will returns
	 * null, when the database is not initialised or the database type is YAML.
	 * 
	 * @return the current database of {@link DBConnector}
	 */
	@Nullable
	DBConnector getDatabase();

	/**
	 * Attempts to connect to the database.
	 */
	void connectDatabase();

	/**
	 * Attempts to load the database. If <code>startup</code> is true or false this
	 * does not affect other process. If this true, only one thing will be loaded on
	 * startup and only once.
	 * 
	 * @param startup whatever some of data should only be loaded on plugin startup
	 */
	void loadDatabase(boolean startup);

	/**
	 * Attempts to save the database
	 */
	void saveDatabase();

	/**
	 * Attempts to convert the database to the given type and completes it. The type
	 * should be the same with {@link DBType}
	 * 
	 * @param type the new type of database
	 * @return true if it success
	 */
	@NotNull
	CompletableFuture<@NotNull Boolean> convertDatabase(@NotNull String type);

	/**
	 * Attempts to load all player statistic from the database.
	 */
	void loadPlayerStatistics();

	/**
	 * Saves all players data to the database.
	 */
	void saveAllPlayerData();

	/**
	 * Attempts to store the given player statistic into database.
	 * 
	 * @param points {@link PlayerPoints}
	 */
	void addPlayerStatistics(@NotNull PlayerPoints points);

	/**
	 * Add points to the given player database.
	 * 
	 * @param points the amount of points
	 * @param uuid   player uuid
	 */
	void addPoints(int points, @NotNull UUID uuid);

	/**
	 * Attempts to retrieve all of players statistic from the current database
	 * asynchronously. If the connection failed, will returns an empty list.
	 * 
	 * @return returns a an ImmutableList of all PlayerPoints that are stored
	 */
	@NotNull
	ImmutableList<@NotNull PlayerPoints> getAllPlayerStatistics();

	/**
	 * Retrieves the all player statistic from the database by the given uuid.
	 * <p>
	 * If the player never played and not found in the database, returns null.
	 * 
	 * @param uuid Player uuid
	 * @return {@link PlayerPoints}
	 */
	@Nullable
	PlayerPoints getPlayerStatsFromData(@NotNull UUID uuid);

	/**
	 * Restores all data of the given player to 0
	 * 
	 * @param uuid UUID of player
	 * @return true if the class inited and player found in database
	 */
	boolean resetPlayerStatistic(@NotNull UUID uuid);

	/**
	 * Attempts to load all players data.
	 * <p>
	 * <b>This is not to be confused with player stats!</b><br>
	 * More understandably, this loads players mixed personal settings, such as
	 * displaying alternating death messages in the game or join delays.
	 */
	void loadMiscPlayersData();

	/**
	 * Attempts to save all players misc data.
	 * <p>
	 * <b>This is not to be confused with player stats!</b><br>
	 * As of described in {@link #loadMiscPlayersData()} its the same thing, but
	 * this method saves the players data in order to be loaded for per-player
	 * handling.
	 */
	void saveMiscPlayersData();
}
