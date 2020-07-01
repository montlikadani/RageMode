package hu.montlikadani.ragemode.gameLogic;

import java.util.List;

import org.bukkit.Location;

public interface IGameSpawn {

	/**
	 * @return {@link Game}
	 */
	Game getGame();

	/**
	 * Check if the game spawns are ready.
	 * 
	 * @return true if ready
	 */
	boolean isReady();

	/**
	 * Gets all spawn locations from list.
	 * 
	 * @return an unmodifiable list
	 */
	List<Location> getSpawnLocations();

	/**
	 * Adds a new spawn location to the list.
	 * 
	 * @param loc The location where we want to save.
	 */
	void addSpawn(Location loc);

	/**
	 * Removes a spawn by location.
	 * 
	 * @param loc The location where we want to remove.
	 */
	void removeSpawn(Location loc);

	/**
	 * Removes all spawns location in game.
	 */
	void removeAllSpawn();

	/**
	 * Checks if have any spawn saved.
	 * 
	 * @return true if have
	 */
	boolean haveAnySpawn();

	/**
	 * Gets a random spawn location from the list. <br>
	 * This will returns <code>null</code> if there are no spawns added to list.
	 * 
	 * @return {@link org.bukkit.Location}
	 */
	Location getRandomSpawn();
}
