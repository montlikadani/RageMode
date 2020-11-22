package hu.montlikadani.ragemode.signs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMSignsUpdateEvent;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class SignCreator {

	private static final Set<SignData> SIGNDATA = new HashSet<>();

	public static boolean loadSigns() {
		SIGNDATA.clear();

		List<String> list = SignConfiguration.getSignConfig().getStringList("signs");
		if (list.isEmpty())
			return false;

		int totalSigns = 0;

		for (String one : list) {
			String[] splited = one.split(",");
			String world = splited[0];
			assert world != null;

			if (Bukkit.getWorld(world) == null) {
				Debug.logConsole(Level.WARNING, "World {0} not found to load this sign.", world);
				continue;
			}

			double x = Double.parseDouble(splited[1]),
					y = Double.parseDouble(splited[2]),
					z = Double.parseDouble(splited[3]);

			String game = splited[4];
			assert game != null;

			Location loc = new Location(Bukkit.getWorld(world), x, y, z);
			SignData data = new SignData(loc, game);

			SIGNDATA.add(data);
			totalSigns++;
		}

		if (totalSigns > 0) {
			Debug.logConsole("Loaded {0} sign{1}.", totalSigns, (totalSigns > 1 ? "s" : ""));
		}

		return true;
	}

	public static boolean createNewSign(Sign sign, String game) {
		FileConfiguration fileConf = SignConfiguration.getSignConfig();
		List<String> signs = fileConf.getStringList("signs");
		String index = locationSignToString(sign.getLocation(), game);
		SignData data = new SignData(sign.getLocation(), game);

		signs.add(index);
		fileConf.set("signs", signs);

		SIGNDATA.add(data);

		Configuration.saveFile(fileConf, SignConfiguration.getSignFile());
		return true;
	}

	public static boolean removeSign(Sign sign) {
		if (SIGNDATA.isEmpty()) {
			return false;
		}

		SignData data = null;
		for (SignData signData : SIGNDATA) {
			if (signData.getLocation().equals(sign.getLocation())) {
				FileConfiguration fileConf = SignConfiguration.getSignConfig();
				List<String> signs = fileConf.getStringList("signs");
				String index = locationSignToString(signData.getLocation(), signData.getGame());

				signs.remove(index);

				fileConf.set("signs", signs);
				Configuration.saveFile(fileConf, SignConfiguration.getSignFile());

				data = signData;
				break;
			}
		}

		if (data != null) {
			return SIGNDATA.remove(data);
		}

		return false;
	}

	/**
	 * Updates a JoinSigns for the given game which are properly configured.
	 * 
	 * @param loc the sign location
	 * @return True if a sign is found on the set location.
	 */
	public static boolean updateSign(Location loc) {
		SignData data = getSignData(loc);
		if (data != null) {
			data.updateSign();
			return true;
		}

		return false;
	}

	/**
	 * Gets the sign data by location.
	 * 
	 * @param loc sign the location
	 * @return {@link SignData} if the location is equal
	 */
	public static SignData getSignData(Location loc) {
		Validate.notNull(loc, "Location can't be null!");

		for (SignData data : SIGNDATA) {
			if (loc.equals(data.getLocation())) {
				return data;
			}
		}

		return null;
	}

	/**
	 * Gets the sign data by game.
	 * 
	 * @param game the game name
	 * @return {@link SignData} if the game is equal
	 */
	public static SignData getSignData(String game) {
		Validate.notEmpty(game, "Game name can't be empty/null");

		for (SignData data : SIGNDATA) {
			if (game.trim().equalsIgnoreCase(data.getGame())) {
				return data;
			}
		}

		return null;
	}

	/**
	 * Updates all ragemode signs for the given game.
	 * 
	 * @param gameName The name of the game for which the signs should be updated.
	 * @return true if at least one sign was updated successfully for the given
	 *         game.
	 */
	public static boolean updateAllSigns(String gameName) {
		Validate.notEmpty(gameName, "Game name can't be empty/null");

		if (!ConfigValues.isSignsEnable()) {
			return false;
		}

		List<String> signs = SignConfiguration.getSignConfig().getStringList("signs");
		if (signs.isEmpty()) {
			return false;
		}

		RMSignsUpdateEvent event = new RMSignsUpdateEvent(SIGNDATA);
		Utils.callEvent(event);
		if (event.isCancelled()) {
			return false;
		}

		for (String signString : signs) {
			String game = getGameFromString(signString);
			if (game == null) {
				continue;
			}

			if (game.equalsIgnoreCase(gameName)) {
				Location signLocation = stringToLocationSign(signString);
				if (signLocation == null) {
					continue;
				}

				Bukkit.getScheduler().callSyncMethod(RageMode.getInstance(), () -> {
					if (signLocation.getBlock().getState() instanceof Sign) {
						SIGNDATA.forEach(SignData::updateSign);
					}

					return true;
				});
			}
		}

		return false;
	}

	/**
	 * Checks whether the given sign is properly configured to be a RageMode Sign.
	 * 
	 * @param loc The sign location
	 * @return true if the block found in the specified location.
	 */
	public static boolean isSign(Location loc) {
		List<String> signs = SignConfiguration.getSignConfig().getStringList("signs");
		if (signs.isEmpty()) {
			return false;
		}

		for (String signString : signs) {
			String game = getGameFromString(signString);
			if (game == null) {
				continue;
			}

			for (String gameName : GetGames.getGameNames()) {
				if (gameName.equalsIgnoreCase(game)) {
					Location signLocation = stringToLocationSign(signString);
					if (signLocation != null && signLocation.getBlock().getState() instanceof Sign
							&& signLocation.equals(loc))
						return true;
				}
			}
		}

		return false;
	}

	/**
	 * Gets the set of SignData
	 * 
	 * @return Set of {@link SignData}
	 */
	public static Set<SignData> getSignData() {
		return SIGNDATA;
	}

	private static String getGameFromString(String raw) {
		return raw.split(",")[4];
	}

	private static String locationSignToString(Location loc, String game) {
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + game;
	}

	private static Location stringToLocationSign(String raw) {
		String[] splited = raw.split(",");
		String world = splited[0];
		double x = Double.parseDouble(splited[1]),
				y = Double.parseDouble(splited[2]),
				z = Double.parseDouble(splited[3]);

		return Bukkit.getWorld(world) != null ? new Location(Bukkit.getWorld(world), x, y, z) : null;
	}
}
