package hu.montlikadani.ragemode;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.API.event.BaseEvent;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;

public class Utils {

	/**
	 * Formats the given time 
	 * @param time the time in decimal
	 * @return formatted time
	 */
	public static String getFormattedTime(long time) {
		String sMark = RageMode.getLang().get("time-formats.second");
		String mMark = RageMode.getLang().get("time-formats.minute");
		if (time < 60)
			return time + sMark;

		long mins = time / 60;
		long remainderSecs = time - (mins * 60);
		if (mins < 60)
			return (mins < 10 ? "0" : "") + mins + mMark + " " + (remainderSecs < 10 ? "0" : "") + remainderSecs + sMark;

		long hours = mins / 60;
		long remainderMins = mins - (hours * 60);
		return (hours < 10 ? "0" : "") + hours + RageMode.getLang().get("time-formats.hour") + " " +
				(remainderMins < 10 ? "0" : "") + remainderMins + mMark + " "
				+ (remainderSecs < 10 ? "0" : "") + remainderSecs + sMark;
	}

	/**
	 * Calls an event.
	 * <p>If the event is Asynchronous then delays the calling, preventing an error.
	 * @param event BaseEvent
	 */
	public static void callEvent(BaseEvent event) {
		if (!hu.montlikadani.ragemode.API.RageModeAPI.getPlugin().isEnabled()) {
			return;
		}

		if (!event.isAsynchronous()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
					() -> Bukkit.getPluginManager().callEvent(event));
		} else {
			Bukkit.getPluginManager().callEvent(event);
		}
	}

	/**
	 * Clears the given player inventory.
	 * @param pl Player
	 */
	public static void clearPlayerInventory(Player pl) {
		pl.getInventory().clear();
		pl.updateInventory();
	}

	/**
	 * Sets the available placeholders to that string, that manages from RageMode
	 * <p>If the <code>fromDatabase</code> false, will gets the statistic from class instance.
	 * @param s String to replace the variables
	 * @param player Player
	 * @return The replaced placeholders
	 */
	public static String setPlaceholders(String s, Player player) {
		java.util.UUID uuid = player.getUniqueId();
		PlayerPoints pp = RuntimePPManager.getPPForPlayer(uuid);
		if (GameUtils.isPlayerPlaying(player)) {
			pp = RageScores.getPlayerPoints(uuid);
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

		PlayerPoints plp = RageMode.getPPFromDatabase(uuid);
		if (s.contains("%games%")) {
			s = s.replace("%games%", plp == null ? "0" : Integer.toString(plp.getGames()));
		}

		if (s.contains("%wins%")) {
			s = s.replace("%wins%", plp == null ? "0" : Integer.toString(plp.getWins()));
		}

		return colors(s);
	}

	/**
	 * Sends the packet to the given player
	 * @param player Player
	 * @param packet Packet name
	 * @throws Exception if something wrong
	 */
	public static void sendPacket(Player player, Object packet) throws Exception {
		Object handle = player.getClass().getMethod("getHandle").invoke(player);
		Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
		playerConnection.getClass().getMethod("sendPacket", new Class[] { getNMSClass("Packet") })
				.invoke(playerConnection, new Object[] { packet });
	}

	/**
	 * Gets the NMS class with packet name
	 * @param name Packet class
	 * @throws ClassNotFoundException if the given class name not found
	 * @return Returns class name from the current version
	 */
	public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + getClassVersion() + "." + name);
	}

	/**
	 * Invoke the IChatBaseComponent
	 * @param name Text to be invoke
	 * @return A json format
	 * @throws Exception if something wrong
	 */
	public static Object getAsIChatBaseComponent(String name) throws Exception {
		Class<?> iChatBaseComponent = getNMSClass("IChatBaseComponent");
		if (ServerVersion.Version.isCurrentEqualOrLower(ServerVersion.Version.v1_8_R2)) {
			Class<?> chatSerializer = getNMSClass("ChatSerializer");
			Method m = chatSerializer.getMethod("a", String.class);
			Object t = iChatBaseComponent.cast(m.invoke(chatSerializer, "{\"text\":\"" + name + "\"}"));
			return t;
		}

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
	 * @return colored text
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