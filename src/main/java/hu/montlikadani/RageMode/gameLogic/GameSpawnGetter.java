package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.MapChecker;

public class GameSpawnGetter {

	private String gameName;
	private boolean isGameReady = false;
	private List<Location> spawnLocations = new ArrayList<>();

	public GameSpawnGetter(String gameName) {
		this.gameName = gameName;

		loadSpawns();
	}

	public void loadSpawns() {
		YamlConfiguration aCfg = RageMode.getInstance().getConfiguration().getArenasCfg();
		if (new MapChecker(gameName).isValid()) {
			String path = "arenas." + gameName + ".spawns";
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

	public void randomSpawn(Player player) {
		Random rand = new Random();
		int x = rand.nextInt(spawnLocations.size() - 1);

		if (!player.getGameMode().equals(GameMode.SPECTATOR))
			player.setHealth(20);

		player.teleport(spawnLocations.get(x));
	}

	public boolean isGameReady() {
		return isGameReady;
	}

	public List<Location> getSpawnLocations() {
		return spawnLocations;
	}
}
