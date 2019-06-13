package hu.montlikadani.ragemode.signs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class SignCreator {

	private static YamlConfiguration fileConf;
	private static SignPlaceholder signPlaceholder = null;

	private static List<SignData> signData = new ArrayList<>();

	public synchronized static boolean loadSigns() {
		fileConf = SignConfiguration.getSignConfig();

		signPlaceholder = new SignPlaceholder(RageMode.getInstance().getConfiguration().getCfg().getStringList("signs.list"));

		List<String> list = fileConf.getStringList("signs");

		if (list == null || list.isEmpty())
			return false;

		int totalSigns = 0;

		for (String one : list) {
			String[] splited = one.split(",");
			String world = splited[0];
			double x = Double.parseDouble(splited[1]);
			double y = Double.parseDouble(splited[2]);
			double z = Double.parseDouble(splited[3]);
			String game = splited[4];

			Location loc = new Location(Bukkit.getWorld(world), x, y, z);
			if (loc != null) {
				SignData data = new SignData(loc, game, signPlaceholder);
				signData.add(data);
			}
			totalSigns += list.size();
		}

		if (totalSigns > 0)
			RageMode.logConsole("[RageMode] Loaded " + totalSigns + " sign" + (totalSigns < 1 ? "s" : "") + ".");

		return true;
	}

	public synchronized static boolean createNewSign(Sign sign, String game) {
		List<String> signs = fileConf.getStringList("signs");
		String index = locationSignToString(sign.getLocation(), game);
		SignData data = new SignData(sign.getLocation(), game, signPlaceholder);

		signs.add(index);
		fileConf.set("signs", signs);
		signData.add(data);
		try {
			fileConf.save(SignConfiguration.getYamlSignsFile());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
			return false;
		}
	}

	public synchronized static boolean removeSign(Sign sign) {
		List<String> signs = fileConf.getStringList("signs");

		for (String game : GetGames.getGameNames()) {
			for (SignData data : signData) {
				if (sign.getLocation().equals(data.getLocation())) {
					String index = locationSignToString(data.getLocation(), game);

					signs.remove(index);
					fileConf.set("signs", signs);
					signData.remove(data);
					try {
						fileConf.save(SignConfiguration.getYamlSignsFile());
						return true;
					} catch (IOException e) {
						e.printStackTrace();
						RageMode.getInstance().throwMsg();
						return false;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Updates a JoinSigns for the given game which are properly configured.
	 * 
	 * @return True if a sign is found on the set location.
	 */
	public static boolean updateSign(Location loc) {
		for (SignData data : signData) {
			if (loc.equals(data.getLocation())) {
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
		if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("signs.enable")) {
			List<String> signs = fileConf.getStringList("signs");
			if (signs != null && !signs.isEmpty()) {
				for (String signString : signs) {
					String game = getGameFromString(signString);
					if (game != null && gameName != null) {
						if (game.trim().contains(gameName.trim())) {
							Location signLocation = stringToLocationSign(signString);
							if (signLocation.getBlock().getState() instanceof Sign) {
								for (SignData data : signData) {
									data.updateSign();
								}
								return true;
							}
						}
					} else
						return false;
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the given sign is properly configured to be an JoinSign or not.
	 * 
	 * @return True if the block found in the specified location.
	 */
	public static boolean isJoinSign(Location loc) {
		List<String> signs = fileConf.getStringList("signs");
		if (signs != null && !signs.isEmpty()) {
			for (String signString : signs) {
				String game = getGameFromString(signString);
				for (String gameName : GetGames.getGameNames()) {
					if (game.trim().contains(gameName.trim())) {
						Location signLocation = stringToLocationSign(signString);
						if (signLocation.getBlock().getState() instanceof Sign && signLocation.equals(loc))
							return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks whether the sign is properly configured to get the game from the sign.
	 * 
	 * @return Game name if found in the list.
	 */
	public static String getGameFromString() {
		List<String> signs = fileConf.getStringList("signs");
		if (signs != null && !signs.isEmpty()) {
			for (String signString : signs) {
				String game = getGameFromString(signString);
				for (String gameName : GetGames.getGameNames()) {
					if (game.trim().contains(gameName.trim()))
						return game;
				}
			}
		}
		return "";
	}

	/**
	 * Gets the SignData list
	 * 
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

		return new Location(Bukkit.getWorld(world), x, y, z);
	}
}
