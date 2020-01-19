package hu.montlikadani.ragemode.gameUtils;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.ConfigValues;

public class GameLobby {

	private static FileConfiguration fi = RageMode.getInstance().getConfiguration().getArenasCfg();

	/**
	 * Gets the given game lobby locations.
	 * @param game Game
	 * @return the lobby location
	 */
	public static Location getLobbyLocation(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		String path = "arenas." + game + ".lobby.";
		String world = fi.getString(path + "world");
		double lobbyX = fi.getDouble(path + "x");
		double lobbyY = fi.getDouble(path + "y");
		double lobbyZ = fi.getDouble(path + "z");
		double lobbyYaw = fi.getDouble(path + "yaw");
		double lobbyPitch = fi.getDouble(path + "pitch");

		Location lobbyLocation = new Location(Bukkit.getWorld(world), lobbyX, lobbyY, lobbyZ, (float) lobbyYaw,
				(float) lobbyPitch);

		return lobbyLocation;
	}

	/**
	 * Get the given game lobby time from file.
	 * @param game Game
	 * @return the lobby time
	 */
	public static int getLobbyTime(String game) {
		int time = 0;
		if (game == null || !fi.isSet("arenas." + game + ".lobbydelay")) {
			time = ConfigValues.getDefaultLobbyDelay() > 0 ? ConfigValues.getDefaultLobbyDelay() : 30;
		} else {
			time = fi.getInt("arenas." + game + ".lobbydelay");
		}

		return time;
	}
}
