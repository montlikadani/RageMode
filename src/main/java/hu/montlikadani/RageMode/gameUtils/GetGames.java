package hu.montlikadani.ragemode.gameUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;

public class GetGames {

	/**
	 * Gets the the total number of games.
	 * <p>This will return 0 if the section not found in the file.
	 * @return total games size
	 */
	public static int getConfigGamesCount() {
		int n = 0;

		FileConfiguration c = RageMode.getInstance().getConfiguration().getArenasCfg();

		if (c.contains("arenas")) {
			n = c.getConfigurationSection("arenas").getKeys(false).size();
		}

		return n;
	}

	/**
	 * Gets the max players size from the file.
	 * <p>This will return -1 if the value not found in the file.
	 * @param game Game
	 * @return the maximum players
	 */
	public static int getMaxPlayers(String game) {
		return RageMode.getInstance().getConfiguration().getArenasCfg().getInt("arenas." + game + ".maxplayers", -1);
	}

	/**
	 * Gets the minimum players size from the file.
	 * <p>This will return -1 if the value not found in the file.
	 * @param game Game
	 * @return the minimum players
	 */
	public static int getMinPlayers(String game) {
		return RageMode.getInstance().getConfiguration().getArenasCfg().getInt("arenas." + game + ".minplayers", -1);
	}

	/**
	 * Get the specified game world name.
	 * @param game Game
	 * @return {@link Optional} world name
	 */
	public static Optional<String> getWorld(String game) {
		return Optional.ofNullable(
				RageMode.getInstance().getConfiguration().getArenasCfg().getString("arenas." + game + ".world"));
	}

	/**
	 * Check if actionbar is enabled in config
	 * @param game Game
	 * @return true if exists and enabled
	 */
	public static boolean isActionbarEnabled(String game) {
		return RageMode.getInstance().getConfiguration().getArenasCfg().getBoolean("arenas." + game + ".actionbar",
				false);
	}

	/**
	 * Check if bossbar is enabled in config
	 * @param game Game
	 * @return true if exists and enabled
	 */
	public static boolean isBossbarEnabled(String game) {
		return RageMode.getInstance().getConfiguration().getArenasCfg().getBoolean("arenas." + game + ".bossbar",
				false);
	}

	/**
	 * Get the game time from config.
	 * @param game Game
	 * @return the game time
	 */
	public static int getGameTime(String game) {
		return RageMode.getInstance().getConfiguration().getArenasCfg().getInt("arenas." + game + ".gametime");
	}

	/**
	 * Adds all game names to an array.
	 * <p>This will returns null if the section not found in the file.
	 * @return the array of game names
	 */
	public static String[] getGameNames() {
		FileConfiguration c = RageMode.getInstance().getConfiguration().getArenasCfg();

		return c.contains("arenas")
				? c.getConfigurationSection("arenas").getKeys(false).toArray(new String[getConfigGamesCount()])
				: null;
	}

	/**
	 * Gets all games converted to list.
	 * <p>This will returns empty list if there are no games found.
	 * @return all games
	 */
	public static List<String> getGames() {
		return getGameNames() != null ? Arrays.asList(getGameNames()) : Collections.emptyList();
	}

	/**
	 * Counts all max players from all games.
	 * @return max players
	 */
	public static int getOverallMaxPlayers() {
		int i = 0, n = 0;
		int x;
		String[] names = getGameNames();
		if (names == null) {
			return 0;
		}

		while (i < names.length) {
			x = RageMode.getInstance().getConfiguration().getArenasCfg().getInt("arenas." + names[i] + ".maxplayers");

			if (n < x) {
				n = x;
			}

			i++;
		}

		return n;
	}

	/**
	 * Check if game is exist that found in the file
	 * @param game Game
	 * @return true if the game is exists
	 */
	public static boolean isGameExistent(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		int i = 0, imax = getConfigGamesCount();
		String[] games = getGameNames();
		if (games == null) {
			return false;
		}

		while (i < imax) {
			if (games[i].equalsIgnoreCase(game.trim())) {
				return true;
			}

			i++;
		}

		return false;
	}
}
