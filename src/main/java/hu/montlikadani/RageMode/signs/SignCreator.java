package hu.montlikadani.ragemode.signs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class SignCreator {

	private static FileConfiguration fileConf = null;
	private static SignPlaceholder signPlaceholder = null;

	private static List<SignData> signData = new ArrayList<>();

	public synchronized static boolean loadSigns() {
		signData.clear();

		if (fileConf == null) {
			fileConf = SignConfiguration.getConf();
		}

		List<String> list = fileConf.getStringList("signs");
		if (list == null || list.isEmpty())
			return false;

		signPlaceholder = new SignPlaceholder(RageMode.getInstance().getConfiguration().getCV().getSignsList());

		int totalSigns = 0;

		for (String one : list) {
			String[] splited = one.split(",");
			String world = splited[0];
			double x = Double.parseDouble(splited[1]);
			double y = Double.parseDouble(splited[2]);
			double z = Double.parseDouble(splited[3]);
			String game = splited[4];

			if (Bukkit.getWorld(world) != null) {
				Location loc = new Location(Bukkit.getWorld(world), x, y, z);
				SignData data = new SignData(loc, game, signPlaceholder);

				signData.add(data);
			} else {
				Debug.logConsole(Level.WARNING, "World " + world + " not found to load this sign.");
				continue;
			}
		}

		totalSigns += list.size();

		if (totalSigns > 0)
			Debug.logConsole("Loaded " + totalSigns + " sign" + (totalSigns > 1 ? "s" : "") + ".");

		return true;
	}

	public synchronized static boolean createNewSign(Sign sign, String game) {
		List<String> signs = fileConf.getStringList("signs");
		String index = locationSignToString(sign.getLocation(), game);
		SignData data = new SignData(sign.getLocation(), game, signPlaceholder);

		signs.add(index);
		fileConf.set("signs", signs);
		signData.add(data);
		Configuration.saveFile(fileConf, SignConfiguration.getFile());
		return true;
	}

	public synchronized static boolean removeSign(Sign sign) {
		if (signData == null || signData.isEmpty())
			return false;

		List<String> signs = fileConf.getStringList("signs");

		for (java.util.Iterator<SignData> it = signData.iterator(); it.hasNext();) {
			SignData data = it.next();
			if (data.getLocation().equals(sign.getLocation())) {
				String index = locationSignToString(data.getLocation(), data.getGame());

				signs.remove(index);
				fileConf.set("signs", signs);
				signData.remove(data);
				Configuration.saveFile(fileConf, SignConfiguration.getFile());
				return true;
			}
		}
		return false;
	}

	/**
	 * Updates a JoinSigns for the given game which are properly configured.
	 * @param loc the sign location
	 * @return True if a sign is found on the set location.
	 */
	public static boolean updateSign(Location loc) {
		if (signData == null || signData.isEmpty())
			return false;

		for (SignData data : signData) {
			if (data.getLocation().equals(loc)) {
				data.updateSign();
				return true;
			}
		}
		return false;
	}

	/**
	 * Updates all JoinSigns for the given game which are properly configured.
	 * 
	 * @param gameName The name of the game for which the signs should be updated.
	 * @return True if at least one sign was updated successfully for the given game.
	 */
	public static boolean updateAllSigns(String gameName) {
		Validate.notNull(gameName, "Game name can't be null!");
		Validate.notEmpty(gameName, "Game name can't be empty!");

		if (RageMode.getInstance().getConfiguration().getCV().isSignsEnable()) {
			List<String> signs = fileConf.getStringList("signs");
			if (signs != null) {
				for (String signString : signs) {
					String game = getGameFromString(signString);
					if (game != null && game.contains(gameName)) {
						Location signLocation = stringToLocationSign(signString);
						if (signLocation != null && signLocation.getBlock().getState() instanceof Sign) {
							signData.forEach(data -> data.updateSign());
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks whether the given sign is properly configured to be an RageMode Sign or not.
	 * @param loc The sign location
	 * @return True if the block found in the specified location.
	 */
	public static boolean isSign(Location loc) {
		List<String> signs = fileConf.getStringList("signs");
		if (signs != null) {
			for (String signString : signs) {
				String game = getGameFromString(signString);
				for (String gameName : GetGames.getGameNames()) {
					if (game.equalsIgnoreCase(gameName)) {
						Location signLocation = stringToLocationSign(signString);
						if (signLocation != null && signLocation.getBlock().getState() instanceof Sign
								&& signLocation.equals(loc))
							return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks whether the sign is properly configured to get the game from the file.
	 * @return Game name if found in the list.
	 */
	public static String getGameFromString() {
		List<String> signs = fileConf.getStringList("signs");
		if (signs != null) {
			for (String signString : signs) {
				String game = getGameFromString(signString);
				for (String gameName : GetGames.getGameNames()) {
					if (game.contains(gameName))
						return game;
				}
			}
		}
		return "";
	}

	/**
	 * Gets the SignData list
	 * @return List of SignData
	 */
	public static List<SignData> getSignData() {
		return signData;
	}

	private static String getGameFromString(String raw) {
		String[] splited = raw.split(",");
		String returnGame = splited[4];
		return returnGame;
	}

	private static String locationSignToString(Location loc, String game) {
		String returnString = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + game;
		return returnString;
	}

	private static Location stringToLocationSign(String raw) {
		String[] splited = raw.split(",");
		String world = splited[0];
		double x = Double.parseDouble(splited[1]);
		double y = Double.parseDouble(splited[2]);
		double z = Double.parseDouble(splited[3]);

		return Bukkit.getWorld(world) != null ? new Location(Bukkit.getWorld(world), x, y, z) : null;
	}
}
