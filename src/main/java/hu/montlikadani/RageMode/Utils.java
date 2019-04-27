package hu.montlikadani.ragemode;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;

public class Utils {

	/**
	 * Time formatting
	 * 
	 * @param time Interval
	 * @return if minute/hour/second less than 10 and writes 0
	 */
	public static String getFormattedTime(int time) {
		String sMark = RageMode.getLang().get("time-formats.second");
		String mMark = RageMode.getLang().get("time-formats.minute");
		if (time < 60)
			return time + sMark;
		else {
			int mins = (int) time / 60;
			int remainderSecs = time - (mins * 60);
			if (mins < 60)
				return (mins < 10 ? "0" : "") + mins + mMark + " " + (remainderSecs < 10 ? "0" : "") + remainderSecs + sMark;
			else {
				int hours = (int) mins / 60;
				int remainderMins = mins - (hours * 60);
				return (hours < 10 ? "0" : "") + hours + RageMode.getLang().get("time-formats.hour") + " " + (remainderMins < 10 ? "0" : "") + remainderMins + mMark + " "
						+ (remainderSecs < 10 ? "0" : "") + remainderSecs + sMark;
			}
		}
	}

	/**
	 * 1.13 fix for default colors not shows in tablist.
	 * 
	 * @param prefix Prefix
	 * @return ChatColor char to show the specified color
	 */
	public static ChatColor fromPrefix(String prefix) {
		char colour = 0;
		char[] chars = prefix.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char at = chars[i];
			if ((at == '\u00a7' || at == '&') && i + 1 < chars.length) {
				char code = chars[i + 1];
				if (ChatColor.getByChar(code) != null)
					colour = code;
			}
		}
		return colour == 0 ? ChatColor.RESET : ChatColor.getByChar(colour);
	}

	/**
	 * Sets the available placeholders to that string, that manages from RageMode
	 * 
	 * @param s String to replaceable the variables
	 * @param player Player
	 * @return String
	 */
	public static String setPlaceholders(String s, Player player) {
		RetPlayerPoints rpp = RuntimeRPPManager.getRPPForPlayer(player.getUniqueId().toString());

		if (s.contains("%kills%"))
			s = s.replace("%kills%", Integer.toString(rpp.getKills()));

		if (s.contains("%axe-kills%"))
			s = s.replace("%axe-kills%", Integer.toString(rpp.getAxeKills()));

		if (s.contains("%direct-arrow-kills%"))
			s = s.replace("%direct-arrow-kills%", Integer.toString(rpp.getDirectArrowKills()));

		if (s.contains("%explosion-kills%"))
			s = s.replace("%explosion-kills%", Integer.toString(rpp.getExplosionKills()));

		if (s.contains("%knife-kills%"))
			s = s.replace("%knife-kills%", Integer.toString(rpp.getKnifeKills()));

		if (s.contains("%deaths%"))
			s = s.replace("%deaths%", Integer.toString(rpp.getDeaths()));

		if (s.contains("%axe-deaths%"))
			s = s.replace("%axe-deaths%", Integer.toString(rpp.getAxeDeaths()));

		if (s.contains("%direct-arrow-deaths%"))
			s = s.replace("%direct-arrow-deaths%", Integer.toString(rpp.getDirectArrowDeaths()));

		if (s.contains("%explosion-deaths%"))
			s = s.replace("%explosion-deaths%", Integer.toString(rpp.getExplosionDeaths()));

		if (s.contains("%knife-deaths%"))
			s = s.replace("%knife-deaths%", Integer.toString(rpp.getKnifeDeaths()));

		if (s.contains("%current-streak%"))
			s = s.replace("%current-streak%", Integer.toString(rpp.getCurrentStreak()));

		if (s.contains("%longest-streak%"))
			s = s.replace("%longest-streak%", Integer.toString(rpp.getLongestStreak()));

		if (s.contains("%points%"))
			s = s.replace("%points%", Integer.toString(rpp.getPoints()));

		if (s.contains("%games%"))
			s = s.replace("%games%", Integer.toString(rpp.getGames()));

		if (s.contains("%wins%"))
			s = s.replace("%wins%", Integer.toString(rpp.getWins()));

		if (s.contains("%kd%"))
			s = s.replace("%kd%", Double.toString(rpp.getKD()));

		return RageMode.getLang().colors(s);
	}

	/**
	 * Sends the packet
	 * 
	 * @param player Player
	 * @param packet Packet name
	 */
	public static void sendPacket(Player player, Object packet) {
		try {
			Object handle = getNMSPlayer(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", new Class[] { getNMSClass("Packet") })
				.invoke(playerConnection, new Object[] { packet });
		} catch (Throwable e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}
	}

	private static Object getNMSPlayer(Player p) throws Throwable {
		return p.getClass().getMethod("getHandle", new Class[0]).invoke(p, new Object[0]);
	}

	/**
	 * Gets the NMS class to support for all versions
	 * 
	 * @param name Packet
	 * @return Returns the MC version
	 */
	public static Class<?> getNMSClass(String name) {
		String version = getPackageVersion();
		try {
			return Class.forName("net.minecraft.server." + version + "." + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the Bukkit package version
	 * <br><br>
	 * Example: v1_8_R3, v1_13_R1
	 * 
	 * @return Package name
	 */
	public static String getPackageVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}

	/**
	 * Gets the Bukkit version
	 * <br><br>
	 * Example: 1.8, 1.13.2
	 * 
	 * @return Bukkit version
	 */
	public static String getVersion() {
		return Bukkit.getVersion();
	}

	/**
	 * Adds to use the color codes in the list
	 * 
	 * @param list List
	 * @return List
	 */
	public static List<String> colorList(List<String> list) {
		List<String> cList = new java.util.ArrayList<>();
		for (String s : list) {
			cList.add(RageMode.getLang().colors(s));
		}
		return cList;
	}

	/**
	 * Check if number is an integer
	 * 
	 * @param str Number as string
	 * @return true if the number as double
	 */
	public static boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	/**
	 * Check if number is an integer
	 * 
	 * @param str Number as string
	 * @return true if the number as integer
	 */
	public static boolean isInt(String str) {
		try {
			Integer.parseInt(str);
		} catch (Throwable e) {
			return false;
		}
		return true;
	}
}