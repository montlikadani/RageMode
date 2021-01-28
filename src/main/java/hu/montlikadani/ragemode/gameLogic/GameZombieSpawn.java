package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;

public class GameZombieSpawn implements IGameSpawn {

	private Game game;

	private boolean isReady = false;
	private final List<Location> spawnLocations = new ArrayList<>();

	public GameZombieSpawn(Game game) {
		this.game = game;

		loadSpawns();
	}

	@Override
	public Game getGame() {
		return game;
	}

	@Override
	public boolean isReady() {
		return isReady;
	}

	@Override
	public List<Location> getSpawnLocations() {
		return Collections.unmodifiableList(spawnLocations);
	}

	private void loadSpawns() {
		removeAllSpawn();

		FileConfiguration conf = RageMode.getInstance().getConfiguration().getArenasCfg();
		String path = "arenas." + game.getName() + ".zombie-spawns";
		if (!conf.isConfigurationSection(path)) {
			return;
		}

		for (String spawnName : conf.getConfigurationSection(path).getKeys(false)) {
			String world = conf.getString(path + "." + spawnName + ".world", "");
			if (world.isEmpty()) {
				continue;
			}

			double spawnX = conf.getDouble(path + "." + spawnName + ".x"),
					spawnY = conf.getDouble(path + "." + spawnName + ".y"),
					spawnZ = conf.getDouble(path + "." + spawnName + ".z"),
					spawnYaw = conf.getDouble(path + "." + spawnName + ".yaw"),
					spawnPitch = conf.getDouble(path + "." + spawnName + ".pitch");

			Location location = new Location(Bukkit.getWorld(world), spawnX, spawnY, spawnZ);
			location.setYaw((float) spawnYaw);
			location.setPitch((float) spawnPitch);

			addSpawn(location);
		}
	}

	@Override
	public boolean addSpawn(Location loc) {
		Validate.notNull(loc, "Location can't be null!");

		return isReady = spawnLocations.add(loc);
	}

	@Override
	public boolean removeSpawn(Location loc) {
		Validate.notNull(loc, "Location can't be null!");

		return spawnLocations.remove(loc);
	}

	@Override
	public void removeAllSpawn() {
		spawnLocations.clear();
		isReady = false;
	}

	@Override
	public boolean haveAnySpawn() {
		return spawnLocations.size() > 0;
	}

	@Override
	public Location getRandomSpawn() {
		return haveAnySpawn() ? spawnLocations.get(ThreadLocalRandom.current().nextInt(spawnLocations.size())) : null;
	}
}
