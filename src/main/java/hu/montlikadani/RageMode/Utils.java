package hu.montlikadani.ragemode;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;

public class Utils {

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
				if (ChatColor.getByChar(code) != null) {
					colour = code;
				}
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
		PlayerPoints pP = new PlayerPoints(player.getUniqueId().toString());
		RetPlayerPoints rpp = new RetPlayerPoints(player.getUniqueId().toString());

		if (s.contains("%kills%"))
			s = s.replace("%kills%", Integer.toString(pP.getKills()));

		if (s.contains("%axe-kills%"))
			s = s.replace("%axe-kills%", Integer.toString(pP.getAxeKills()));

		if (s.contains("%direct-arrow-kills%"))
			s = s.replace("%direct-arrow-kills%", Integer.toString(pP.getDirectArrowKills()));

		if (s.contains("%explosion-kills%"))
			s = s.replace("%explosion-kills%", Integer.toString(pP.getExplosionKills()));

		if (s.contains("%knife-kills%"))
			s = s.replace("%knife-kills%", Integer.toString(pP.getKnifeKills()));

		if (s.contains("%deaths%"))
			s = s.replace("%deaths%", Integer.toString(pP.getDeaths()));

		if (s.contains("%axe-deaths%"))
			s = s.replace("%axe-deaths%", Integer.toString(pP.getAxeDeaths()));

		if (s.contains("%direct-arrow-deaths%"))
			s = s.replace("%direct-arrow-deaths%", Integer.toString(pP.getDirectArrowDeaths()));

		if (s.contains("%explosion-deaths%"))
			s = s.replace("%explosion-deaths%", Integer.toString(pP.getExplosionDeaths()));

		if (s.contains("%knife-deaths%"))
			s = s.replace("%knife-deaths%", Integer.toString(pP.getKnifeDeaths()));

		if (s.contains("%current-streak%"))
			s = s.replace("%current-streak%", Integer.toString(pP.getCurrentStreak()));

		if (s.contains("%longest-streak%"))
			s = s.replace("%longest-streak%", Integer.toString(pP.getLongestStreak()));

		if (s.contains("%points%"))
			s = s.replace("%points%", Integer.toString(pP.getPoints()));

		if (s.contains("%games%"))
			s = s.replace("%games%", Integer.toString(rpp.getGames()));

		if (s.contains("%wins%"))
			s = s.replace("%wins%", Integer.toString(rpp.getWins()));

		if (s.contains("%kd%"))
			s = s.replace("%kd%", String.valueOf(rpp.getKD()));

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
			playerConnection.getClass().getMethod("sendPacket", new Class[] { getNMSClass("Packet") }).invoke(playerConnection, new Object[] { packet });
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
		String version = RageMode.getPackageVersion();
		try {
			return Class.forName("net.minecraft.server." + version + "." + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}