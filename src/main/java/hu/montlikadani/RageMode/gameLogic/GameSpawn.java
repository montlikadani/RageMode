package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;

public class GameSpawn {

	private Game game;
	private boolean isGameReady = false;
	private List<Location> spawnLocations = new ArrayList<>();

	public GameSpawn(Game game) {
		this.game = game;

		loadSpawns();
	}

	private void loadSpawns() {
		removeAllSpawn();

		FileConfiguration aCfg = RageMode.getInstance().getConfiguration().getArenasCfg();
		String path = "arenas." + game.getName() + ".spawns";

		for (String spawnName : aCfg.getConfigurationSection(path).getKeys(false)) {
			String world = aCfg.getString(path + "." + spawnName + ".world");
			double spawnX = aCfg.getDouble(path + "." + spawnName + ".x");
			double spawnY = aCfg.getDouble(path + "." + spawnName + ".y");
			double spawnZ = aCfg.getDouble(path + "." + spawnName + ".z");
			double spawnYaw = aCfg.getDouble(path + "." + spawnName + ".yaw");
			double spawnPitch = aCfg.getDouble(path + "." + spawnName + ".pitch");

			Location location = new Location(Bukkit.getWorld(world), spawnX, spawnY, spawnZ);
			location.setYaw((float) spawnYaw);
			location.setPitch((float) spawnPitch);

			addSpawn(location);
		}
	}

	/**
	 * Adds a new spawn location to the list.
	 * @param loc The location where we want to save.
	 */
	public void addSpawn(Location loc) {
		Validate.notNull(loc, "Location can't be null!");

		if (spawnLocations.add(loc)) {
			isGameReady = true;
		}
	}

	/**
	 * Removes a spawn by location.
	 * @param loc The location where we want to remove.
	 */
	public void removeSpawn(Location loc) {
		Validate.notNull(loc, "Location can't be null!");

		for (Iterator<Location> it = spawnLocations.iterator(); it.hasNext();) {
			if (it.next().equals(loc)) {
				it.remove();
				break;
			}
		}
	}

	/**
	 * Removes all spawns location in game.
	 */
	public void removeAllSpawn() {
		spawnLocations.clear();
	}

	/**
	 * Teleports the given player to a random spawn location in game.
	 * This will returns if spawns are not set correctly.
	 * @param player Player
	 */
	@Deprecated
	public void randomSpawn(Player player) {
		if (spawnLocations.size() > 0) {
			int x = ThreadLocalRandom.current().nextInt(spawnLocations.size());
			player.teleport(spawnLocations.get(x));
		}
	}

	/**
	 * Gets a random spawn location from the list.
	 * <br>This will returns <code>null</code> if there are no spawns added to list.
	 * @return {@link org.bukkit.Location}
	 */
	public Location getRandomSpawn() {
		if (spawnLocations.size() < 0) {
			return null; // Do NOT use isEmpty() due to getting the 0 element
		}

		int x = ThreadLocalRandom.current().nextInt(spawnLocations.size());
		return spawnLocations.get(x);
	}

	public Game getGame() {
		return game;
	}

	/**
	 * Check if the game spawns are ready.
	 * @return true if ready
	 */
	public boolean isGameReady() {
		return isGameReady;
	}

	/**
	 * Gets all spawn locations from list.
	 * @return an unmodifiable list
	 */
	public List<Location> getSpawnLocations() {
		return Collections.unmodifiableList(spawnLocations);
	}
}
