package hu.montlikadani.ragemode.gameUtils;

import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

public class MapChecker {

	private String gameName;
	private boolean isValid = false;
	private String message = "";
	private int maxPlayers;

	public MapChecker(String gameName) {
		Validate.notNull(gameName, "Game name can't be null!");
		Validate.notEmpty(gameName, "Game name can't be empty!");

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
			return;
		}

		if (!GameUtils.checkName(null, gameName)) {
			message = RageMode.getLang().get("bad-ragemode-name");
			isValid = false;
			return;
		}

		isValid = true;
	}

	private void checkBasics() {
		String path = "arenas." + gameName;
		if (!RageMode.getInstance().getConfiguration().getArenasCfg().isSet(path + ".maxplayers")
				|| !RageMode.getInstance().getConfiguration().getArenasCfg().isSet(path + ".world")) {
			message = RageMode.getLang().get("game.name-or-maxplayers-not-set");
			isValid = false;
			return;
		}

		if (!RageMode.getInstance().getConfiguration().getArenasCfg().contains(path + ".world")
				|| GetGames.getWorld(gameName).isEmpty()) {
			message = RageMode.getLang().get("game.worldname-not-set");
			isValid = false;
			return;
		}

		maxPlayers = GetGames.getMaxPlayers(gameName);
		if (maxPlayers < 0) {
			message = RageMode.getLang().get("game.maxplayers-not-set", "%game%", gameName);
			isValid = false;
			return;
		}

		isValid = true;
	}

	private void checkLobby() {
		FileConfiguration aFile = RageMode.getInstance().getConfiguration().getArenasCfg();

		if (!aFile.isSet("arenas." + gameName + ".lobby")) {
			message = RageMode.getLang().get("setup.lobby.not-set", "%game%", gameName);
			isValid = false;
			return;
		}

		String thisPath = "arenas." + gameName + ".lobby";
		if (aFile.isSet(thisPath + ".x") && aFile.isSet(thisPath + ".y") && aFile.isSet(thisPath + ".z")
				&& aFile.isSet(thisPath + ".yaw") && aFile.isSet(thisPath + ".pitch")) {
			if (!aFile.contains(thisPath + ".world") || GetGames.getWorld(gameName).isEmpty()) {
				message = RageMode.getLang().get("setup.lobby.worldname-not-set");
				isValid = false;
				return;
			}

			if (Utils.isDouble(aFile.getString(thisPath + ".x")) && Utils.isDouble(aFile.getString(thisPath + ".y"))
					&& Utils.isDouble(aFile.getString(thisPath + ".z"))
					&& Utils.isDouble(aFile.getString(thisPath + ".yaw"))
					&& Utils.isDouble(aFile.getString(thisPath + ".pitch")))
				isValid = true;
			else {
				message = RageMode.getLang().get("setup.lobby.coords-not-set");
				isValid = false;
				return;
			}
		} else {
			message = RageMode.getLang().get("setup.lobby.not-set-properly");
			isValid = false;
		}
	}

	private void checkSpawns() {
		FileConfiguration aFile = RageMode.getInstance().getConfiguration().getArenasCfg();
		String path = "arenas." + gameName + ".spawns";
		if (!aFile.isSet(path)) {
			message = RageMode.getLang().get("game.no-spawns-configured", "%game%", gameName);
			isValid = false;
			return;
		}

		Set<String> spawnNames = aFile.getConfigurationSection(path).getKeys(false);
		if (spawnNames.size() <= maxPlayers) {
			message = RageMode.getLang().get("game.too-few-spawns");
			isValid = false;
			return;
		}

		for (String s : spawnNames) {
			World world = null;
			try {
				world = org.bukkit.Bukkit.getWorld(aFile.getString(path + "." + s + ".world"));
			} catch (Exception e) {
				isValid = false;
				world = null;
			}
			if (world != null && Utils.isDouble(aFile.getString(path + "." + s + ".x"))
					&& Utils.isDouble(aFile.getString(path + "." + s + ".y"))
					&& Utils.isDouble(aFile.getString(path + "." + s + ".z"))
					&& Utils.isDouble(aFile.getString(path + "." + s + ".yaw"))
					&& Utils.isDouble(aFile.getString(path + "." + s + ".pitch")))
				isValid = true;
			else {
				message = RageMode.getLang().get("game.spawns-not-set-properly");
				isValid = false;
				break;
			}
		}
	}

	public boolean isValid() {
		return isValid;
	}

	public String getMessage() {
		return message;
	}

	public static boolean isGameWorld(String gameName, World world) {
		Validate.notNull(gameName, "Game name can't be null!");
		Validate.notEmpty(gameName, "Game name can't be empty!");
		Validate.notNull(world, "World can't be null!");

		FileConfiguration aFile = RageMode.getInstance().getConfiguration().getArenasCfg();
		String spawnsPath = "arenas." + gameName + ".spawns";

		Set<String> allSpawns = aFile.getConfigurationSection(spawnsPath).getKeys(false);
		for (String spawn : allSpawns) {
			if (aFile.getString(spawnsPath + "." + spawn + ".world").trim().equals(world.getName().trim())) {
				return true;
			}
		}

		String worldName = GameLobby.getLobbyLocation(gameName).getWorld().getName();
		if (worldName.trim().equals(world.getName().trim()))
			return true;

		return false;
	}
}
