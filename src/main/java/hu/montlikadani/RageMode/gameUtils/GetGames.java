package hu.montlikadani.ragemode.gameUtils;

import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;

public class GetGames {

	private static FileConfiguration fi = RageMode.getInstance().getConfiguration().getArenasCfg();

	public static int getConfigGamesCount() {
		int n = 0;

		if (!RageMode.getInstance().getConfiguration().getArenasFile().exists() ||
				!fi.contains("arenas") || fi.getConfigurationSection("arenas").getKeys(false) == null)
			return 0;
		n = fi.getConfigurationSection("arenas").getKeys(false).size();
		return n;
	}

	public static int getMaxPlayers(String game) {
		if (!fi.isSet("arenas." + game + ".maxplayers"))
			return -1;
		else
			return fi.getInt("arenas." + game + ".maxplayers");
	}

	public static String[] getGameNames() {
		String[] names = new String[getConfigGamesCount()];

		if (fi.contains("arenas") && fi.getConfigurationSection("arenas").getKeys(false) != null)
			fi.getConfigurationSection("arenas").getKeys(false).toArray(names);
		return names;
	}

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
