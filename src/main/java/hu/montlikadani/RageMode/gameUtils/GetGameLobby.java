package hu.montlikadani.ragemode.gameUtils;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;

public class GetGameLobby {

	private static FileConfiguration fi = RageMode.getInstance().getConfiguration().getArenasCfg();

	/**
	 * Gets the specified game lobby location.
	 * @param game Game
	 * @return Lobby location
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
	 * Get the specified game lobby time from file.
	 * @param game Game
	 * @return time
	 */
	public static int getLobbyTime(String game) {
		int time = 0;
		if (game == null || !fi.isSet("arenas." + game + ".lobbydelay")) {
			time = RageMode.getInstance().getConfiguration().getCV().getLobbyDelay() > 0
					? RageMode.getInstance().getConfiguration().getCV().getLobbyDelay()
					: 30;
		} else
			time = fi.getInt("arenas." + game + ".lobbydelay");

		return time;
	}
}
