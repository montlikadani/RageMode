package hu.montlikadani.ragemode.gameUtils;

import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.RageMode;

public class GetGames {

	private static YamlConfiguration fi = RageMode.getInstance().getConfiguration().getArenasCfg();

	/**
	 * Get arenas games count
	 * 
	 * @return int games count
	 */
	public static int getConfigGamesCount() {
		int n = 0;

		if (RageMode.getInstance().getConfiguration().getArenasFile().exists() && fi.contains("arenas"))
			n = fi.getConfigurationSection("arenas").getKeys(false).size();

		return n;
	}

	/**
	 * Get max players from a file
	 * 
	 * @param game Game
	 * @return int number
	 */
	public static int getMaxPlayers(String game) {
		return !fi.isSet("arenas." + game + ".maxplayers") ? -1 : fi.getInt("arenas." + game + ".maxplayers");
	}

	/**
	 * Get game names from file
	 * 
	 * @return config section
	 */
	public static String[] getGameNames() {
		return fi.getConfigurationSection("arenas").getKeys(false).toArray(new String[getConfigGamesCount()]);
	}

	/**
	 * Gets overall max players from file
	 * 
	 * @return int number
	 */
	public static int getOverallMaxPlayers() {
		int i = 0;
		int n = 0;
		int x;
		String[] names = getGameNames();
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
	 * 
	 * @param game Game
	 * @return true if correct
	 */
	public static boolean isGameExistent(String game) {
		int i = 0;
		int imax = getConfigGamesCount();
		String[] games = getGameNames();
		while (i < imax) {
			if (games[i].equals(game))
				return true;
			i++;
		}
		return false;

	}
}
