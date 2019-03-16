package hu.montlikadani.ragemode.signs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class SignCreator {

	private static FileConfiguration fileConf = SignConfiguration.getSignConfiguration();
	private static SignPlaceholder signPlaceholder;

	private static List<SignData> signData = new ArrayList<>();

	public synchronized static boolean createNewSign(Sign sign, String game) {
		List<String> signs = fileConf.getStringList("signs");
		String index = locationSignToString(sign.getLocation(), game);

		signPlaceholder = new SignPlaceholder(RageMode.getInstance().getConfiguration().getCfg().getStringList("signs.list"));

		signs.add(index);
		fileConf.set("signs", signs);
		try {
			fileConf.save(SignConfiguration.getYamlSignsFile());

			if (signPlaceholder != null) {
				SignData data = new SignData(sign.getLocation(), game, signPlaceholder);
				signData.add(data);
			}
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
			String index = locationSignToString(sign.getLocation(), game);

			signPlaceholder = new SignPlaceholder(RageMode.getInstance().getConfiguration().getCfg().getStringList("signs.list"));

			signs.remove(index);
			fileConf.set("signs", signs);
			try {
				fileConf.save(SignConfiguration.getYamlSignsFile());

				if (signPlaceholder != null) {
					SignData data = new SignData(sign.getLocation(), game, signPlaceholder);
					signData.remove(data);
				}
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				RageMode.getInstance().throwMsg();
				return false;
			}
		}
		return false;
	}

	/**
	 * Updates a JoinSigns for the given game which are properly configured.
	 * 
	 * @param event Event that are called.
	 * @return True if a sign is found on the set location.
	 */
	public static boolean updateSign(Sign sign) {
		List<String> signs = fileConf.getStringList("signs");

		if (signs != null && !signs.isEmpty()) {
			for (String list : signs) {
				String game = getGameFromString(list);

				if (signPlaceholder == null)
					signPlaceholder = new SignPlaceholder();
				else {
					SignData data = new SignData(sign.getLocation(), game, signPlaceholder);

					data.updateSign();
				}
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
								if (signPlaceholder == null)
									signPlaceholder = new SignPlaceholder();
								else {
									SignData data = new SignData(signLocation, game, signPlaceholder);

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
	public static boolean isJoinSign() {
		List<String> signs = fileConf.getStringList("signs");
		if (signs != null && !signs.isEmpty()) {
			for (String signString : signs) {
				String game = getGameFromString(signString);
				for (String gameName : GetGames.getGameNames()) {
					if (game.trim().contains(gameName.trim())) {
						Location signLocation = stringToLocationSign(signString);
						if (signLocation.getBlock().getState() instanceof Sign)
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
	 * @return True if the block found in the specified game.
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
