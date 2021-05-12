package hu.montlikadani.ragemode.gameLogic.spawn;

import java.util.List;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hu.montlikadani.ragemode.gameLogic.Game;

/**
 * Interface to handle game spawns
 */
public interface IGameSpawn {

	/**
	 * @return the {@link Game} of this spawn
	 */
	@NotNull
	Game getGame();

	/**
	 * Check if the game spawns are ready.
	 * 
	 * @return true if ready
	 */
	boolean isReady();

	/**
	 * Returns all spawn locations in an unmodifiable list.
	 * 
	 * @return an unmodifiable list
	 */
	@NotNull
	List<Location> getSpawnLocations();

	/**
	 * Adds a new spawn location to the list.
	 * 
	 * @param loc The {@link Location} where to save
	 * @return true if added
	 */
	boolean addSpawn(@NotNull Location loc);

	/**
	 * Removes a spawn by location.
	 * 
	 * @param loc The {@link Location} where to remove.
	 * @return true if removed
	 */
	boolean removeSpawn(@NotNull Location loc);

	/**
	 * Removes all cached spawns locations.
	 */
	void removeAllSpawn();

	/**
	 * Checks if have any spawn saved.
	 * 
	 * @return true if have
	 */
	boolean haveAnySpawn();

	/**
	 * Gets a random spawn location from the list.
	 * 
	 * @return {@link org.bukkit.Location}
	 */
	@Nullable
	Location getRandomSpawn();
}
