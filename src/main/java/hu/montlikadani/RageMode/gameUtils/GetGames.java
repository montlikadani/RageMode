package hu.montlikadani.ragemode.gameUtils;

import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;

public class GetGames {

	private static FileConfiguration fi = RageMode.getInstance().getConfiguration().getArenasCfg();

	/**
	 * Get arenas games count
	 * <p>This will return 0 if the section not found in the file.
	 * @return int games count
	 */
	public static int getConfigGamesCount() {
		int n = 0;

		if (fi.contains("arenas"))
			n = fi.getConfigurationSection("arenas").getKeys(false).size();

		return n;
	}

	/**
	 * Get max players from a file
	 * <p>This will return -1 if the value not found in the file.
	 * @param game Game
	 * @return int number
	 */
	public static int getMaxPlayers(String game) {
		return !fi.isSet("arenas." + game + ".maxplayers") ? -1 : fi.getInt("arenas." + game + ".maxplayers");
	}

	/**
	 * Get the specified game world
	 * @param game Game
	 * @return World name
	 */
	public static String getWorld(String game) {
		return !fi.isSet("arenas." + game + ".world") ? null : fi.getString("arenas." + game + ".world");
	}

	/**
	 * Check if actionbar is enabled in config
	 * @param game Game
	 * @return actionbar boolean
	 */
	public static boolean isActionbarEnabled(String game) {
		return fi.getBoolean("arenas." + game + ".actionbar");
	}

	/**
	 * Check if bossbar is enabled in config
	 * @param game Game
	 * @return bossbar boolean
	 */
	public static boolean isBossbarEnabled(String game) {
		return fi.getBoolean("arenas." + game + ".bossbar");
	}

	/**
	 * Get the game time from config
	 * @param game Game
	 * @return time
	 */
	public static int getGameTime(String game) {
		return fi.getInt("arenas." + game + ".gametime");
	}

	/**
	 * Get game names from file
	 * <p>This will returns null if the section not found in the file.
	 * @return config section
	 */
	public static String[] getGameNames() {
		return fi.contains("arenas") ? fi.getConfigurationSection("arenas").getKeys(false).toArray(new String[getConfigGamesCount()])
				: null;
	}

	/**
	 * Gets all games converted to list.
	 * @return all games
	 */
	public static List<String> getGames() {
		return Arrays.asList(getGameNames());
	}

	/**
	 * Gets overall max players from file
	 * @return max players integer
	 */
	public static int getOverallMaxPlayers() {
		int i = 0;
		int n = 0;
		int x;
		String[] names = getGameNames();
		if (names == null)
			return 0;

		while (i < names.length) {
			x = fi.getInt("arenas." + names[i] + ".maxplayers");
			if (n < x)
				n = x;
			i++;
		}
		return n;
	}

	/**
	 * Check if game is exist that found in the file
	 * @param game Game
	 * @return true if correct
	 */
	public static boolean isGameExistent(String game) {
		int i = 0;
		int imax = getConfigGamesCount();
		String[] games = getGameNames();
		while (i < imax) {
			if (games[i].equalsIgnoreCase(game))
				return true;
			i++;
		}
		return false;
	}
}
