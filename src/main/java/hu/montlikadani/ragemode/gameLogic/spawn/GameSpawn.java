package hu.montlikadani.ragemode.gameLogic.spawn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;

public class GameSpawn implements IGameSpawn {

	private BaseGame game;

	private final List<Location> spawnLocations = new ArrayList<>();

	public GameSpawn(BaseGame game) {
		this.game = game;

		loadSpawns();
	}

	@Override
	public BaseGame getGame() {
		return game;
	}

	@Override
	public boolean isReady() {
		return spawnLocations.size() >= game.maxPlayers;
	}

	@Override
	public List<Location> getSpawnLocations() {
		return Collections.unmodifiableList(spawnLocations);
	}

	private void loadSpawns() {
		removeAllSpawn();

		org.bukkit.configuration.MemorySection conf = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class)
				.getConfiguration().getArenasCfg();

		org.bukkit.configuration.ConfigurationSection section = conf
				.getConfigurationSection("arenas." + game.getName() + ".spawns");
		if (section == null) {
			return;
		}

		for (String spawnName : section.getKeys(false)) {
			String worldName = section.getString(spawnName + ".world", "");

			if (worldName.isEmpty()) {
				continue;
			}

			org.bukkit.World world = Bukkit.getWorld(worldName);
			if (world == null) {
				continue;
			}

			double spawnX = section.getDouble(spawnName + ".x"), spawnY = section.getDouble(spawnName + ".y"),
					spawnZ = section.getDouble(spawnName + ".z"), spawnYaw = section.getDouble(spawnName + ".yaw"),
					spawnPitch = section.getDouble(spawnName + ".pitch");

			Location location = new Location(world, spawnX, spawnY, spawnZ);
			location.setYaw((float) spawnYaw);
			location.setPitch((float) spawnPitch);

			addSpawn(location);
		}
	}

	@Override
	public boolean addSpawn(Location loc) {
		Validate.notNull(loc, "Location can't be null!");

		return spawnLocations.add(loc);
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
		return spawnLocations.size() > 0;
	}

	@Override
	public Location getRandomSpawn() {
		int size = spawnLocations.size();
		return size == 0 ? null : spawnLocations.get(size == 1 ? 0 : ThreadLocalRandom.current().nextInt(size));
	}
}
