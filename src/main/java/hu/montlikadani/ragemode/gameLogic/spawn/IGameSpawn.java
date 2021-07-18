package hu.montlikadani.ragemode.gameLogic.spawn;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hu.montlikadani.ragemode.gameLogic.base.BaseGame;

/**
 * Interface to handle game spawns
 */
public interface IGameSpawn {

	/**
	 * @return the {@link BaseGame} of this spawn
	 */
	@NotNull
	BaseGame getGame();

	/**
	 * Check if the game spawns are ready.
	 * 
	 * @return true if ready
	 */
	boolean isReady();

	/**
	 * Returns a list of spawn locations in an unmodifiable list.
	 * 
	 * @return an unmodifiable list of location
	 */
	@NotNull
	java.util.List<@NotNull Location> getSpawnLocations();

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
