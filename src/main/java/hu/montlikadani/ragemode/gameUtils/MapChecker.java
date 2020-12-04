package hu.montlikadani.ragemode.gameUtils;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.area.GameArea;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.config.Configuration;

public class MapChecker {

	private String gameName;
	private boolean isValid = false;
	private String message = "";
	private int maxPlayers;

	public MapChecker(String gameName) {
		Validate.notEmpty(gameName, "Game name can't be empty/null");

		this.gameName = gameName;

		checkMapName();
		if (isValid)
			checkBasics();
		if (isValid)
			checkLobby();
		if (isValid)
			checkSpawns();
		if (isValid)
			checkArea();
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
		isValid = false;

		Configuration conf = RageMode.getInstance().getConfiguration();
		String path = "arenas." + gameName;
		if (!conf.getArenasCfg().isSet(path + ".maxplayers") || !conf.getArenasCfg().isSet(path + ".world")) {
			message = RageMode.getLang().get("game.name-or-maxplayers-not-set");
			return;
		}

		if (!conf.getArenasCfg().contains(path + ".world") || !GetGames.getWorld(gameName).isPresent()) {
			message = RageMode.getLang().get("game.worldname-not-set");
			return;
		}

		maxPlayers = GetGames.getMaxPlayers(gameName);
		if (maxPlayers < 0) {
			message = RageMode.getLang().get("game.maxplayers-not-set", "%game%", gameName);
			return;
		}

		boolean changed = false;
		int minPlayers = GetGames.getMinPlayers(gameName);
		if (maxPlayers < minPlayers) {
			Debug.logConsole(Level.WARNING, "Minimum players can't greater than maximum players. Attempting to modify...");
			maxPlayers = minPlayers;
			changed = true;
		}

		if (minPlayers < 1) {
			minPlayers = 2;
			changed = true;
		}

		if (changed) {
			conf.getArenasCfg().set(path + ".minplayers", minPlayers);
			Configuration.saveFile(conf.getArenasCfg(), conf.getArenasFile());
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
			if (!aFile.contains(thisPath + ".world") || !GetGames.getWorld(gameName).isPresent()) {
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
		if (!aFile.isConfigurationSection(path)) {
			message = RageMode.getLang().get("game.no-spawns-configured", "%game%", gameName);
			isValid = false;
			return;
		}

		Set<String> spawnNames = aFile.getConfigurationSection(path).getKeys(false);
		if (spawnNames.size() < maxPlayers) {
			message = RageMode.getLang().get("game.too-few-spawns");
			isValid = false;
			return;
		}

		for (String s : spawnNames) {
			World world = Bukkit.getWorld(aFile.getString(path + "." + s + ".world", ""));
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

		hu.montlikadani.ragemode.gameLogic.Game game = GameUtils.getGame(gameName);
		if (game != null && game.getGameType() == GameType.APOCALYPSE) {
			path = "arenas." + gameName + ".zombie-spawns";
			if (!aFile.isSet(path)) {
				message = RageMode.getLang().get("game.no-spawns-configured", "%game%", gameName);
				isValid = false;
				return;
			}

			for (String s : aFile.getConfigurationSection(path).getKeys(false)) {
				World world = Bukkit.getWorld(aFile.getString(path + "." + s + ".world", ""));
				if (world != null && Utils.isDouble(aFile.getString(path + "." + s + ".x"))
						&& Utils.isDouble(aFile.getString(path + "." + s + ".y"))
						&& Utils.isDouble(aFile.getString(path + "." + s + ".z")))
					isValid = true;
				else {
					message = RageMode.getLang().get("game.spawns-not-set-properly");
					isValid = false;
					break;
				}
			}
		}
	}

	private void checkArea() {
		isValid = false;

		if (!RageMode.getInstance().getConfiguration().getAreasFile().exists()) {
			message = RageMode.getLang().get("game.areas-not-set");
			return;
		}

		FileConfiguration areaC = RageMode.getInstance().getConfiguration().getAreasCfg();
		if (!areaC.isConfigurationSection("areas")) {
			message = RageMode.getLang().get("game.areas-not-set");
			return;
		}

		for (Map.Entry<String, GameArea> map : GameAreaManager.getGameAreas().entrySet()) {
			// TODO: Add a check for area to make sure that is set in the correct position
			if (map.getValue().getGame().getName().equalsIgnoreCase(gameName)) {
				isValid = true;
				break;
			}
		}

		if (!isValid) {
			message = RageMode.getLang().get("game.areas-not-set");
		}
	}

	public boolean isValid() {
		return isValid;
	}

	public String getMessage() {
		return message;
	}

	public static boolean isGameWorld(String gameName, World world) {
		Validate.notEmpty(gameName, "Game name can't be empty/null");
		Validate.notNull(world, "World can't be null");

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
