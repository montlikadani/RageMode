package hu.montlikadani.ragemode;

import java.io.File;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.API.event.BaseEvent;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;

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
	 * Calls an event.
	 * <p>If the event is Asynchronous then delays the calling, preventing an error.
	 * @param event BaseEvent
	 */
	public static void callEvent(BaseEvent event) {
		if (!event.isAsynchronous()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
					() -> Bukkit.getPluginManager().callEvent(event));
		} else {
			Bukkit.getPluginManager().callEvent(event);
		}
	}

	/**
	 * Clear the specified player inventory.
	 * @param pl Player
	 */
	public static void clearPlayerInventory(Player pl) {
		pl.getInventory().clear();
		pl.updateInventory();
	}

	/**
	 * Sets the available placeholders to that string, that manages from RageMode
	 * @param s String to replace the variables
	 * @param player Player
	 * @return String
	 */
	public static String setPlaceholders(String s, Player player) {
		return setPlaceholders(s, player, false);
	}

	/**
	 * Sets the available placeholders to that string, that manages from RageMode
	 * @param s String to replace the variables
	 * @param player Player
	 * @param fromDatabase true - get stats from database, false - from class instance
	 * @return String
	 */
	public static String setPlaceholders(String s, Player player, boolean fromDatabase) {
		PlayerPoints pp = null;
		if (fromDatabase) {
			pp = RuntimePPManager.getPPFromDatabase(player.getUniqueId().toString());
		} else {
			pp = RuntimePPManager.getPPForPlayer(player.getUniqueId().toString());
		}

		if (s.contains("%kills%")) {
			int kills = pp == null ? 0 : pp.getKills();
			s = s.replace("%kills%", Integer.toString(kills));
		}

		if (s.contains("%axe-kills%")) {
			int axeKills = pp == null ? 0 : pp.getAxeKills();
			s = s.replace("%axe-kills%", Integer.toString(axeKills));
		}

		if (s.contains("%direct-arrow-kills%")) {
			int directArrowKills = pp == null ? 0 : pp.getDirectArrowKills();
			s = s.replace("%direct-arrow-kills%", Integer.toString(directArrowKills));
		}

		if (s.contains("%explosion-kills%")) {
			int explosionKills = pp == null ? 0 : pp.getExplosionKills();
			s = s.replace("%explosion-kills%", Integer.toString(explosionKills));
		}

		if (s.contains("%knife-kills%")) {
			int knifeKills = pp == null ? 0 : pp.getKnifeKills();
			s = s.replace("%knife-kills%", Integer.toString(knifeKills));
		}

		if (s.contains("%deaths%")) {
			int deaths = pp == null ? 0 : pp.getDeaths();
			s = s.replace("%deaths%", Integer.toString(deaths));
		}

		if (s.contains("%axe-deaths%")) {
			int axeDeaths = pp == null ? 0 : pp.getAxeDeaths();
			s = s.replace("%axe-deaths%", Integer.toString(axeDeaths));
		}

		if (s.contains("%direct-arrow-deaths%")) {
			int directArrowDeaths = pp == null ? 0 : pp.getDirectArrowDeaths();
			s = s.replace("%direct-arrow-deaths%", Integer.toString(directArrowDeaths));
		}

		if (s.contains("%explosion-deaths%")) {
			int explosionDeaths = pp == null ? 0 : pp.getExplosionDeaths();
			s = s.replace("%explosion-deaths%", Integer.toString(explosionDeaths));
		}

		if (s.contains("%knife-deaths%")) {
			int knifeDeaths = pp == null ? 0 : pp.getKnifeDeaths();
			s = s.replace("%knife-deaths%", Integer.toString(knifeDeaths));
		}

		if (s.contains("%kd%")) {
			double kd = pp == null ? 0d : pp.getKD();
			NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
			s = s.replace("%kd%", format.format(kd));
		}

		if (s.contains("%current-streak%"))
			s = s.replace("%current-streak%", pp == null ? "0" : Integer.toString(pp.getCurrentStreak()));

		if (s.contains("%longest-streak%"))
			s = s.replace("%longest-streak%", pp == null ? "0" : Integer.toString(pp.getLongestStreak()));

		if (s.contains("%points%"))
			s = s.replace("%points%", pp == null ? "0" : Integer.toString(pp.getPoints()));

		if (s.contains("%games%"))
			s = s.replace("%games%", pp == null ? "0" : Integer.toString(pp.getGames()));

		if (s.contains("%wins%"))
			s = s.replace("%wins%", pp == null ? "0" : Integer.toString(pp.getWins()));

		if (s.contains("%rank%"))
			s = s.replace("%rank%", pp == null ? "0" : Integer.toString(pp.getRank()));

		return colors(s);
	}

	/**
	 * Gets all classes in the specified package.
	 * @param packageName where to find the classes
	 * @return All classes in list
	 */
	public static List<Class<?>> getClasses(String packageName) {
		List<Class<?>> classes = new ArrayList<>();
		try {
			JarFile file = new JarFile(
					new File(RageMode.getInstance().getFolder().getParentFile().getPath(),
						RageMode.getInstance().getDescription().getName() + ".jar"));
			for (Enumeration<JarEntry> entry = file.entries(); entry.hasMoreElements();) {
				JarEntry jarEntry = entry.nextElement();
				String name = jarEntry.getName().replace("/", ".");
				if (name.startsWith(packageName) && name.endsWith(".class")) {
					classes.add(Class.forName(name.substring(0, name.length() - 6)));
				}
			}

			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return classes;
	}

	/**
	 * Sends the packet to the specified player
	 * @param player Player
	 * @param packet Packet name
	 * @throws Exception if class/method not found
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
		List<String> cList = new ArrayList<>();
		for (String s : list) {
			cList.add(colors(s));
		}
		return cList;
	}

	/**
	 * Adds to use color codes in the string.
	 * @param s String
	 * @return colored textt
	 */
	public static String colors(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
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