package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.toolbox.MapChecker;

public class GameSpawnGetter {

	private String gameName;
	private boolean isGameReady = false;
	private List<Location> spawnLocations = new ArrayList<>();

	public GameSpawnGetter(String gameName) {
		this.gameName = gameName;

		getSpawns();
	}

	private void getSpawns() {
		FileConfiguration aCfg = RageMode.getInstance().getConfiguration().getArenasCfg();
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

	public boolean isGameReady() {
		return isGameReady;
	}

	public List<Location> getSpawnLocations() {
		return spawnLocations;
	}
}
