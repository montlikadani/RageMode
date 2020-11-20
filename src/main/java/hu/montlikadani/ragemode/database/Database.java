package hu.montlikadani.ragemode.database;

import java.util.List;
import java.util.UUID;

import hu.montlikadani.ragemode.scores.PlayerPoints;

public interface Database {

	/**
	 * Returns the current connected database. In some situations this will returns
	 * null, when the database is not initialised or the database type is YAML.
	 * 
	 * @return the current database of {@link DBConnector}
	 */
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
	 * Attempts to convert the database to the given type. The type should be the
	 * same as in {@link DBType}
	 * 
	 * @param type the new type of database
	 * @return true if it success
	 */
	boolean convertDatabase(String type);

	/**
	 * Attempts to load all player statistic from the database.
	 */
	void loadPlayerStatistics();

	/**
	 * Saves all players data to the database.
	 */
	void saveData();

	/**
	 * Attempts to store the given player statistic into database.
	 * 
	 * @param points {@link PlayerPoints}
	 */
	void addPlayerStatistics(PlayerPoints points);

	/**
	 * Add points to the given player database.
	 * 
	 * @param points the amount of points
	 * @param uuid   player uuid
	 */
	void addPoints(int points, UUID uuid);

	/**
	 * Attempts to retrieve all of players statistic from database. This does not
	 * loads the saved player statistic, only retrieves the uuid and trying to
	 * instantiate a new object if not exists, but not includes into database.
	 * 
	 * @return returns a List of all PlayerPoints that are stored
	 */
	List<PlayerPoints> getAllPlayerStatistics();

	/**
	 * Retrieves the all player statistic from the database by the given uuid.
	 * <p>
	 * If the player never played and not found in the database, returns null.
	 * 
	 * @param uuid Player uuid
	 * @return {@link PlayerPoints}
	 */
	PlayerPoints getPlayerStatsFromData(UUID uuid);

	/**
	 * Restores all data of the given player to 0
	 * 
	 * @param uuid UUID of player
	 * @return true if the class inited and player found in database
	 */
	boolean resetPlayerStatistic(UUID uuid);

	/**
	 * Attempts to load join delay from database.
	 */
	void loadJoinDelay();

	/**
	 * Attempts to save all join delay data into database.
	 */
	void saveJoinDelay();
}
