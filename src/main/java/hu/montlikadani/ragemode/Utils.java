package hu.montlikadani.ragemode;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.ServerVersion.Version;
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
	 * <p>If the event is Asynchronous then runs in the main thread, preventing an error.
	 * @param event BaseEvent
	 */
	public static void callEvent(BaseEvent event) {
		if (!hu.montlikadani.ragemode.API.RageModeAPI.getPlugin().isEnabled()) {
			return;
		}

		if (!event.isAsynchronous()) {
			Bukkit.getScheduler().callSyncMethod(RageMode.getInstance(), () -> {
				Bukkit.getPluginManager().callEvent(event);
				return true;
			});
		} else {
			Bukkit.getPluginManager().callEvent(event);
		}
	}

	public static void teleportSync(Player player, Location loc) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () -> player.teleport(loc));
	}

	public static Collection<Entity> getNearbyEntities(org.bukkit.World w, Location loc, int radius) {
		return Version.isCurrentHigher(Version.v1_8_R1) ? w.getNearbyEntities(loc, radius, radius, radius)
				: getNearbyList(loc, radius);
	}

	public static List<Entity> getNearbyEntities(org.bukkit.entity.Entity e, int radius) {
		return Version.isCurrentHigher(Version.v1_8_R1) ? e.getNearbyEntities(radius, radius, radius)
				: getNearbyList(e.getLocation(), radius);
	}

	private static List<Entity> getNearbyList(Location loc, int radius) {
		int chunkRadius = radius < 16 ? 1 : radius / 16;
		List<Entity> radiusEntities = new java.util.ArrayList<>();

		for (int chunkX = 0 - chunkRadius; chunkX <= chunkRadius; chunkX++) {
			for (int chunkZ = 0 - chunkRadius; chunkZ <= chunkRadius; chunkZ++) {
				int x = (int) loc.getX(), y = (int) loc.getY(), z = (int) loc.getZ();

				for (Entity e : new Location(loc.getWorld(), x + chunkX * 16, y, z + chunkZ * 16).getChunk()
						.getEntities()) {
					if (!(loc.getWorld().getName().equalsIgnoreCase(e.getWorld().getName()))) {
						continue;
					}

					if (e.getLocation().distanceSquared(loc) <= radius * radius
							&& e.getLocation().getBlock() != loc.getBlock()) {
						radiusEntities.add(e);
					}
				}
			}
		}

		return radiusEntities;
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
	 * Sets the available placeholders to that string, that manages from RageMode.<br><br>
	 * If the player is playing currently it will replaces the placeholders to the current score.
	 * @param s String to replace the variables
	 * @param pp {@link PlayerPoints}
	 * @see #setPlaceholders(String, Player)
	 * @return The replaced placeholders
	 */
	public static String setPlaceholders(String s, PlayerPoints pp) {
		return setPlaceholders(s, Bukkit.getPlayer(pp.getUUID()));
	}

	/**
	 * Sets the available placeholders to that string, that manages from RageMode.<br><br>
	 * If the player is playing currently it will replaces the placeholders to the current score.
	 * @param s String to replace the variables
	 * @param player Player
	 * @return The replaced placeholders
	 */
	public static String setPlaceholders(String s, Player player) {
		PlayerPoints pp = null;
		java.util.UUID uuid = null;

		if (player != null) {
			uuid = player.getUniqueId();
			pp = RuntimePPManager.getPPForPlayer(uuid);

			if (GameUtils.isPlayerPlaying(player)) {
				pp = RageScores.getPlayerPoints(uuid).orElse(null);
			}
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

		if (s.contains("%zombie-kills%")) {
			int zombieKills = pp == null ? 0 : pp.getZombieKills();
			s = s.replace("%zombie-kills%", Integer.toString(zombieKills));
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
			s = s.replace("%games%", Integer.toString(plp == null ? pp == null ? 0 : pp.getGames() : plp.getGames()));
		}

		if (s.contains("%wins%")) {
			s = s.replace("%wins%", Integer.toString(plp == null ? pp == null ? 0 : pp.getWins() : plp.getWins()));
		}

		return colors(s);
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
		return list.stream().map(Utils::colors).collect(Collectors.toList());
	}

	/**
	 * Adds to use color codes in the string.
	 * @param s String
	 * @return colored text
	 */
	public static String colors(String s) {
		if (s == null) {
			return "";
		}

		if (s.contains("#") && Version.isCurrentEqualOrHigher(Version.v1_16_R1)) {
			s = matchColorRegex(s);
		}

		return ChatColor.translateAlternateColorCodes('&', s);
	}

	private static String matchColorRegex(String s) {
		String regex = "&?#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})";
		Matcher matcher = Pattern.compile(regex).matcher(s);
		while (matcher.find()) {
			String group = matcher.group(0);
			String group2 = matcher.group(1);

			try {
				s = s.replace(group, net.md_5.bungee.api.ChatColor.of("#" + group2) + "");
			} catch (Exception e) {
				Debug.logConsole(java.util.logging.Level.WARNING, "Bad hex color: " + group);
			}
		}

		return s;
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

	public abstract static class Reflections {

		public static void sendPacket(Player player, Object packet) throws Exception {
			Object handle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
		}

		public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
			return Class.forName("net.minecraft.server." + getClassVersion() + "." + name);
		}

		public static Object getAsIChatBaseComponent(String name) throws Exception {
			Class<?> iChatBaseComponent = getNMSClass("IChatBaseComponent");
			if (Version.isCurrentEqualOrLower(Version.v1_8_R2)) {
				Class<?> chatSerializer = getNMSClass("ChatSerializer");
				Method m = chatSerializer.getMethod("a", String.class);
				Object t = iChatBaseComponent.cast(m.invoke(chatSerializer, "{\"text\":\"" + name + "\"}"));
				return t;
			}

			Class<?> declaredClass = iChatBaseComponent.getDeclaredClasses()[0];
			Method m = declaredClass.getMethod("a", String.class);
			return m.invoke(iChatBaseComponent, "{\"text\":\"" + name + "\"}");
		}

		public static String getClassVersion() {
			return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		}

		public static int getCurrentJavaVersion() {
			String currentVersion = System.getProperty("java.version");
			if (currentVersion.contains("_")) {
				currentVersion = currentVersion.split("_")[0];
			}

			currentVersion = currentVersion.replaceAll("[^\\d]|_", "");

			for (int i = 8; i <= 18; i++) {
				if (currentVersion.contains(Integer.toString(i))) {
					return i;
				}
			}

			return 0;
		}
	}
}