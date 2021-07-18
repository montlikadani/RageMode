package hu.montlikadani.ragemode.holder.holograms;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * An abstract class which represents the hologram editing
 */
public abstract class IHoloHolder {

	/**
	 * Displays a new hologram to the given location and saves into config.
	 * 
	 * @param loc {@link Location}
	 */
	public abstract void addHolo(Location loc);

	/**
	 * Loads all holograms from configuration and displays for all online players.
	 */
	public abstract void loadHolos();

	/**
	 * Updates all the holograms name for the given player.
	 * 
	 * @param player {@link Player}
	 */
	public abstract void updateHologramsName(Player player);

	/**
	 * Deletes hologram(s) at the given location.
	 * 
	 * @param loc {@link Location}
	 * @return true if at least one hologram at the given location was removed
	 */
	public abstract boolean deleteHologram(Location loc);

	/**
	 * Deletes all the holograms that was added before.
	 * 
	 * @param removeFromFile whether to remove from file too
	 */
	public abstract void deleteAllHologram(boolean removeFromFile);

	/**
	 * Gets the closest hologram around the given location.
	 * 
	 * @param location {@link Location}
	 * @return {@link ArmorStands} if present, otherwise {@link Optional#empty()}
	 */
	public abstract Optional<ArmorStands> getClosest(Location location);

}
