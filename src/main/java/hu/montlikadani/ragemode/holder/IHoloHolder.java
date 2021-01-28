package hu.montlikadani.ragemode.holder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * An abstract class which represents the hologram editing
 */
public abstract class IHoloHolder {

	protected static final List<Location> HOLOS = new ArrayList<>();

	/**
	 * Displays a new hologram to the given location and sets into config.
	 * 
	 * @param loc {@link Location}
	 */
	public abstract void addHolo(Location loc);

	/**
	 * Loads all holos from configuration and displays for all online players
	 */
	public abstract void loadHolos();

	/**
	 * Displays the holo for the given player at the given location.
	 * 
	 * @param player {@link Player}
	 * @param loc    {@link Location}
	 */
	public abstract void displayHoloToPlayer(Player player, Location loc);

	/**
	 * Deletes the hologram at the given location.
	 * 
	 * @param loc {@link Location}
	 * @return true if the hologram exist with the given location
	 */
	public abstract boolean deleteHologram(Location loc);

	/**
	 * Deletes all holograms that was added before.
	 */
	public abstract void deleteAllHologram();

	/**
	 * Deletes the hologram from the given player.
	 * 
	 * @param player {@link Player}
	 */
	public abstract void deleteHoloObjectsOfPlayer(Player player);

	/**
	 * Getting the closest hologram around the player.
	 * 
	 * @param <T>    The object of {@link ArmorStands} or
	 *               {@link com.gmail.filoghost.holographicdisplays.api.Hologram}
	 * 
	 * @param player Player
	 * @param eyeLoc player eye location or current location
	 * @return {@link Optional}
	 */
	public abstract <T> Optional<T> getClosest(Player player, boolean eyeLoc);

	/**
	 * Displays all holos for the given player.
	 * 
	 * @param player {@link Player}
	 */
	public abstract void showAllHolosToPlayer(Player player);

	/**
	 * Updates all holos for the given player.
	 * 
	 * @param player {@link Player}
	 */
	public abstract void updateHolosForPlayer(Player player);
}
