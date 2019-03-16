package hu.montlikadani.ragemode.gameUtils;

import java.util.Set;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;

public class MapChecker {

	private String gameName;
	private boolean isValid = false;
	private String message = "";
	private int maxPlayers;

	public MapChecker(String gameName) {
		if (gameName == null) {
			throw new NullPointerException("Game name cannot be null!");
		}
		this.gameName = gameName;

		checkMapName();
		if (isValid)
			checkBasics();
		if (isValid)
			checkLobby();
		if (isValid)
			checkSpawns();
	}

	private void checkMapName() {
		if (!RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + gameName)) {
			message = RageMode.getLang().get("invalid-game", "%game%", gameName);
			isValid = false;
		} else
			isValid = true;
	}

	private void checkBasics() {
		String path = "arenas." + gameName;
		if (!RageMode.getInstance().getConfiguration().getArenasCfg().isSet(path + ".maxplayers") ||
				!RageMode.getInstance().getConfiguration().getArenasCfg().isSet(path + ".world")) {
			message = RageMode.getLang().get("game.name-or-maxplayers-not-set");
			isValid = false;
		} else {
			if (RageMode.getInstance().getConfiguration().getArenasCfg().contains(path + ".world") &&
					!RageMode.getInstance().getConfiguration().getArenasCfg().getString(path + ".world").equals("")) {
				maxPlayers = RageMode.getInstance().getConfiguration().getArenasCfg().getInt(path + ".maxplayers");
				if (maxPlayers != -32500000) {
					isValid = true;
				} else {
					message = RageMode.getLang().get("game.maxplayers-not-set", "%game%", gameName);
					isValid = false;
				}
			} else {
				message = RageMode.getLang().get("game.worldname-not-set");
				isValid = false;
			}
		}
	}

	private void checkLobby() {
		FileConfiguration aFile = RageMode.getInstance().getConfiguration().getArenasCfg();

		if (!aFile.isSet("arenas." + gameName + ".lobby")) {
			message = RageMode.getLang().get("setup.lobby.not-set", "%game%", gameName);
			isValid = false;
		} else {
			String thisPath = "arenas." + gameName + ".lobby";
			if (aFile.isSet(thisPath + ".x") && aFile.isSet(thisPath + ".y")
					&& aFile.isSet(thisPath + ".z")
					&& aFile.isSet(thisPath + ".yaw") && aFile.isSet(thisPath + ".pitch")) {
				if (aFile.contains(thisPath + ".world") && !aFile.getString(thisPath + ".world").equals("")) {
					if (isDouble(aFile.getString(thisPath + ".x")) && isDouble(aFile.getString(thisPath + ".y"))
							&& isDouble(aFile.getString(thisPath + ".z"))
							&& isDouble(aFile.getString(thisPath + ".yaw"))
							&& isDouble(aFile.getString(thisPath + ".pitch")))
						isValid = true;
					else {
						message = RageMode.getLang().get("setup.lobby.coords-not-set");
						isValid = false;
						return;
					}
				} else {
					message = RageMode.getLang().get("setup.lobby.worldname-not-set");
					isValid = false;
				}
			} else {
				message = RageMode.getLang().get("setup.lobby.not-set-properly");
				isValid = false;
			}
		}
	}

	private void checkSpawns() {
		FileConfiguration aFile = RageMode.getInstance().getConfiguration().getArenasCfg();
		String path = "arenas." + gameName;
		if (aFile.isSet(path + ".spawns")) {
			Set<String> spawnNames = aFile.getConfigurationSection(path + ".spawns").getKeys(false);
			if (spawnNames.size() >= maxPlayers) {
				for (String s : spawnNames) {
					World world = null;
					try {
						world = org.bukkit.Bukkit.getWorld(aFile.getString(path + ".spawns." + s + ".world"));
					} catch (Throwable e) {
						isValid = false;
						world = null;
					}
					if (world != null
							&& isDouble(aFile.getString(path + ".spawns." + s + ".x"))
							&& isDouble(aFile.getString(path + ".spawns." + s + ".y"))
							&& isDouble(aFile.getString(path + ".spawns." + s + ".z"))
							&& isDouble(aFile.getString(path + ".spawns." + s + ".yaw"))
							&& isDouble(aFile.getString(path + ".spawns." + s + ".pitch")))
						isValid = true;
					else {
						message = RageMode.getLang().get("game.spawns-not-set-properly");
						isValid = false;
						break;
					}
				}
			} else {
				message = RageMode.getLang().get("game.too-few-spawns");
				isValid = false;
			}
		} else {
			message = RageMode.getLang().get("game.no-spawns-configured", "%game%", gameName);
			isValid = false;
		}
	}

	private boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	public boolean isValid() {
		return isValid;
	}

	public String getMessage() {
		return message;
	}

	public static boolean isGameWorld(String gameName, World world) {
		if (gameName == null) {
			throw new NullPointerException("Game name cannot be null!");
		}
		if (world == null) {
			throw new NullPointerException("World cannot be null!");
		}
		String spawnsPath = "arenas." + gameName + ".spawns";
		Set<String> allSpawns = RageMode.getInstance().getConfiguration().getArenasCfg().getConfigurationSection(spawnsPath).getKeys(false);
		for (String spawn : allSpawns) {
			String worldName = RageMode.getInstance().getConfiguration().getArenasCfg().getString(spawnsPath + "." + spawn + ".world");
			if (worldName.trim().equals(world.getName().trim()))
				return true;
		}

		String worldName = RageMode.getInstance().getConfiguration().getArenasCfg().getString("arenas." + gameName + ".lobby.world");
		if (worldName.trim().equals(world.getName().trim()))
			return true;

		return false;
	}
}
