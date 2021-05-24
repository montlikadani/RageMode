package hu.montlikadani.ragemode.signs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.API.event.RMSignsUpdateEvent;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.utils.Debug;
import hu.montlikadani.ragemode.utils.SchedulerUtil;
import hu.montlikadani.ragemode.utils.Utils;

public final class SignCreator {

	private static final Set<SignData> SIGNDATA = new HashSet<>();

	public static void loadSigns() {
		SIGNDATA.clear();

		List<String> list = SignConfiguration.getSignConfig().getStringList("signs");
		if (list.isEmpty())
			return;

		for (String one : list) {
			String[] splitted = one.split(",", 5);

			if (splitted.length < 5) {
				continue;
			}

			World world = Bukkit.getWorld(splitted[0]);
			if (world == null) {
				Debug.logConsole(Level.WARNING, "World {0} is not exist to load this sign.", splitted[0]);
				continue;
			}

			double x = Utils.tryParse(splitted[1]).orElse(0d), y = Utils.tryParse(splitted[2]).orElse(0d),
					z = Utils.tryParse(splitted[3]).orElse(0d);

			SIGNDATA.add(new SignData(new Location(world, x, y, z), splitted[4]));
		}

		int totalSigns = SIGNDATA.size();
		if (totalSigns > 0) {
			Debug.logConsole("Loaded {0} sign{1}.", totalSigns, (totalSigns > 1 ? "s" : ""));
		}

		return;
	}

	public static boolean createNewSign(Sign sign, String game) {
		FileConfiguration fileConf = SignConfiguration.getSignConfig();
		List<String> signs = fileConf.getStringList("signs");
		Location loc = sign.getLocation();

		signs.add(locationSignToString(loc, game));
		fileConf.set("signs", signs);

		SIGNDATA.add(new SignData(loc, game));

		Configuration.saveFile(fileConf, SignConfiguration.getSignFile());
		return true;
	}

	public static boolean removeSign(SignData signData) {
		if (signData == null) {
			return false;
		}

		FileConfiguration fileConf = SignConfiguration.getSignConfig();
		List<String> signs = fileConf.getStringList("signs");

		signs.remove(locationSignToString(signData.getLocation(), signData.getGameName()));
		fileConf.set("signs", signs);

		Configuration.saveFile(fileConf, SignConfiguration.getSignFile());

		return SIGNDATA.remove(signData);
	}

	/**
	 * Updates the given sign content from the location.
	 * 
	 * @param loc the sign {@link Location}
	 * @return True if the sign is found at the given location, otherwise false
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
	 * @return {@link SignData} if the given location is same, or null if not
	 */
	public static SignData getSignData(Location loc) {
		if (loc != null) {
			for (SignData data : SIGNDATA) {
				if (data.getLocation().equals(loc)) {
					return data;
				}
			}
		}

		return null;
	}

	/**
	 * Gets the sign data by game name.
	 * 
	 * @param game the game name
	 * @return {@link SignData} if the given game name is same, or null if not
	 */
	public static SignData getSignData(String game) {
		if (game != null && !game.isEmpty()) {
			for (SignData data : SIGNDATA) {
				if (game.equalsIgnoreCase(data.getGameName())) {
					return data;
				}
			}
		}

		return null;
	}

	/**
	 * Updates all ragemode signs for the given game.
	 * 
	 * @param gameName The name of the game for which the signs should be updated.
	 * @return true if at least one sign was updated successfully for the given
	 *         game, otherwise false.
	 */
	public static boolean updateAllSigns(String gameName) {
		org.apache.commons.lang.Validate.notEmpty(gameName, "Game name can't be empty/null");

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

		boolean updated = false;

		for (String signString : signs) {
			if (getGameFromString(signString).equalsIgnoreCase(gameName)) {
				Location signLocation = stringToLocationSign(signString);

				if (signLocation == null) {
					continue;
				}

				SchedulerUtil.submitSync(() -> {
					if (signLocation.getBlock().getState() instanceof Sign) {
						SIGNDATA.forEach(SignData::updateSign);
					}

					return true;
				});

				updated = true;
			}
		}

		return updated;
	}

	/**
	 * Returns the cached set of SignData.
	 * 
	 * @return Set of {@link SignData}
	 */
	public static Set<SignData> getSignData() {
		return SIGNDATA;
	}

	private static String getGameFromString(String raw) {
		return raw.split(",", 5)[4];
	}

	private static String locationSignToString(Location loc, String game) {
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + game;
	}

	private static Location stringToLocationSign(String raw) {
		String[] splitted = raw.split(",", 4);

		if (splitted.length < 4) {
			return null;
		}

		World world = Bukkit.getWorld(splitted[0]);
		double x = Utils.tryParse(splitted[1]).orElse(0d), y = Utils.tryParse(splitted[2]).orElse(0d),
				z = Utils.tryParse(splitted[3]).orElse(0d);

		return world != null ? new Location(world, x, y, z) : null;
	}
}
