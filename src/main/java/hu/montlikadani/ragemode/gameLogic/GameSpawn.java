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

public class GameSpawn implements IGameSpawn {

	private Game game;

	private boolean isReady = false;
	private final List<Location> spawnLocations = new ArrayList<>();

	public GameSpawn(Game game) {
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

		isReady = false;

		FileConfiguration conf = RageMode.getInstance().getConfiguration().getArenasCfg();
		String path = "arenas." + game.getName() + ".spawns";
		if (!conf.contains(path)) {
			return;
		}

		for (String spawnName : conf.getConfigurationSection(path).getKeys(false)) {
			String world = conf.getString(path + "." + spawnName + ".world");
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
	}

	@Override
	public boolean haveAnySpawn() {
		// Do NOT use isEmpty() due to getting the 0 element
		return spawnLocations.size() > 0;
	}

	@Override
	public Location getRandomSpawn() {
		if (!haveAnySpawn()) {
			return null;
		}

		int x = ThreadLocalRandom.current().nextInt(spawnLocations.size());
		return spawnLocations.get(x);
	}
}
