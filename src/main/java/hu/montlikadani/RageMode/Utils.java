package hu.montlikadani.ragemode;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;

public class Utils {

	/**
	 * Time formatting
	 * @param time Interval
	 * @return if minute/hour/second less than 10 and writes 0
	 */
	public static String getFormattedTime(int time) {
		String sMark = RageMode.getLang().get("time-formats.second");
		String mMark = RageMode.getLang().get("time-formats.minute");
		if (time < 60)
			return time + sMark;

		int mins = time / 60;
		int remainderSecs = time - (mins * 60);
		if (mins < 60)
			return (mins < 10 ? "0" : "") + mins + mMark + " " + (remainderSecs < 10 ? "0" : "") + remainderSecs + sMark;

		int hours = mins / 60;
		int remainderMins = mins - (hours * 60);
		return (hours < 10 ? "0" : "") + hours + RageMode.getLang().get("time-formats.hour") + " " +
				(remainderMins < 10 ? "0" : "") + remainderMins + mMark + " "
				+ (remainderSecs < 10 ? "0" : "") + remainderSecs + sMark;
	}

	/**
	 * 1.13 fix for default colors not shows in tablist.
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
	 * Clear the specified player inventory.
	 * <p>This fixes the crash when the inventory is empty and the
	 * server throws AssertionError: TRAP, that cause the server crash and stop.
	 * <p><b>Regenerate world should fix this.</b>
	 * @param pl Player
	 */
	public static void clearPlayerInventory(Player pl) {
		try {
			for (ItemStack content : pl.getInventory().getContents()) {
				if (content != null && !content.getType().equals(Material.AIR))
					pl.getInventory().clear();
			}
		} catch (AssertionError a) {}
	}

	/**
	 * Sets the available placeholders to that string, that manages from RageMode
	 * @param s String to replace the variables
	 * @param player Player
	 * @return String
	 */
	public static String setPlaceholders(String s, Player player) {
		RetPlayerPoints rpp = RuntimeRPPManager.getRPPForPlayer(player.getUniqueId().toString());

		if (s.contains("%kills%"))
			s = s.replace("%kills%", rpp == null ? "0" : Integer.toString(rpp.getKills()));

		if (s.contains("%axe-kills%"))
			s = s.replace("%axe-kills%", rpp == null ? "0" : Integer.toString(rpp.getAxeKills()));

		if (s.contains("%direct-arrow-kills%"))
			s = s.replace("%direct-arrow-kills%", rpp == null ? "0" : Integer.toString(rpp.getDirectArrowKills()));

		if (s.contains("%explosion-kills%"))
			s = s.replace("%explosion-kills%", rpp == null ? "0" : Integer.toString(rpp.getExplosionKills()));

		if (s.contains("%knife-kills%"))
			s = s.replace("%knife-kills%", rpp == null ? "0" : Integer.toString(rpp.getKnifeKills()));

		if (s.contains("%deaths%"))
			s = s.replace("%deaths%", rpp == null ? "0" : Integer.toString(rpp.getDeaths()));

		if (s.contains("%axe-deaths%"))
			s = s.replace("%axe-deaths%", rpp == null ? "0" : Integer.toString(rpp.getAxeDeaths()));

		if (s.contains("%direct-arrow-deaths%"))
			s = s.replace("%direct-arrow-deaths%", rpp == null ? "0" : Integer.toString(rpp.getDirectArrowDeaths()));

		if (s.contains("%explosion-deaths%"))
			s = s.replace("%explosion-deaths%", rpp == null ? "0" : Integer.toString(rpp.getExplosionDeaths()));

		if (s.contains("%knife-deaths%"))
			s = s.replace("%knife-deaths%", rpp == null ? "0" : Integer.toString(rpp.getKnifeDeaths()));

		if (s.contains("%current-streak%"))
			s = s.replace("%current-streak%", rpp == null ? "0" : Integer.toString(rpp.getCurrentStreak()));

		if (s.contains("%longest-streak%"))
			s = s.replace("%longest-streak%", rpp == null ? "0" : Integer.toString(rpp.getLongestStreak()));

		if (s.contains("%points%"))
			s = s.replace("%points%", rpp == null ? "0" : Integer.toString(rpp.getPoints()));

		if (s.contains("%games%"))
			s = s.replace("%games%", rpp == null ? "0" : Integer.toString(rpp.getGames()));

		if (s.contains("%wins%"))
			s = s.replace("%wins%", rpp == null ? "0" : Integer.toString(rpp.getWins()));

		if (s.contains("%kd%")) {
			NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
			s = s.replace("%kd%", rpp == null ? "0.0" : format.format(rpp.getKD()));
		}

		return RageMode.getLang().colors(s);
	}

	/**
	 * Sends the packet to the specified player
	 * @param player Player
	 * @param packet Packet name
	 * @throws Throwable if class/method not found
	 */
	public static void sendPacket(Player player, Object packet) throws Exception {
		Object handle = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
		Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
		playerConnection.getClass().getMethod("sendPacket", new Class[] { getNMSClass("Packet") })
			.invoke(playerConnection, new Object[] { packet });
	}

	/**
	 * Gets the NMS class with packet name
	 * @param name Packet class
	 * @throws ClassNotFoundException if class not found
	 * @return Returns class name from the current version
	 */
	public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + getClassVersion() + "." + name);
	}

	/**
	 * Invoke the IChatBaseComponent
	 * @param name String
	 * @return JSON
	 * @throws Exception if class/method not found
	 */
	public static Object getAsIChatBaseComponent(String name) throws Exception {
		Class<?> iChatBaseComponent = getNMSClass("IChatBaseComponent");
		Class<?> declaredClass = iChatBaseComponent.getDeclaredClasses()[0];
		Method m = declaredClass.getMethod("a", String.class);
		return m.invoke(iChatBaseComponent, "{\"text\":\"" + name + "\"}");
	}

	/**
	 * Gets the current class mc version.
	 * @return version
	 */
	public static String getClassVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	}

	/**
	 * Gets the Bukkit version
	 * <p>Example: 1.8, 1.13.2
	 * @return Bukkit version
	 */
	public static String getVersion() {
		return Bukkit.getVersion();
	}

	/**
	 * Adds to use the color codes in the list
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
	 * Gets the Bukkit RGB color from string
	 * @param paramString String
	 * @return Bukkit RGB color from string
	 */
	public static Color getColorFromString(String paramString) {
		String[] color = paramString.split(",");
		return Color.fromRGB(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
	}

	/**
	 * Check if number is an integer
	 * @param str Number as string
	 * @return true if the number as double
	 */
	public static boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * Check if number is an integer
	 * @param str Number as string
	 * @return true if the number as integer
	 */
	public static boolean isInt(String str) {
		try {
			Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
}