package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.MapChecker;

public class GameSpawnGetter {

	private Game game;
	private boolean isGameReady = false;
	private List<Location> spawnLocations = new ArrayList<>();

	public GameSpawnGetter(Game game) {
		this.game = game;

		loadSpawns();
	}

	private void loadSpawns() {
		FileConfiguration aCfg = RageMode.getInstance().getConfiguration().getArenasCfg();
		if (new MapChecker(game.getName()).isValid()) {
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

				spawnLocations.add(location);
			}
			isGameReady = true;
		} else
			isGameReady = false;
	}

	/**
	 * Teleports the specified player to a random location in game.
	 * This will not execute if spawns are not set correctly.
	 * @param player Player
	 */
	public void randomSpawn(Player player) {
		if (spawnLocations.size() > 0) {
			Random rand = new Random();
			int x = rand.nextInt(spawnLocations.size());

			player.teleport(spawnLocations.get(x));
		}
	}

	public Game getGame() {
		return game;
	}

	public boolean isGameReady() {
		return isGameReady;
	}

	public List<Location> getSpawnLocations() {
		return spawnLocations;
	}
}
