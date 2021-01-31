package hu.montlikadani.ragemode;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;

public class Utils {

	public static String getFormattedTime(long time) {
		String sMark = RageMode.getLang().get("time-formats.second");
		String mMark = RageMode.getLang().get("time-formats.minute");
		if (time < 60)
			return time + sMark;

		long mins = time / 60;
		long remainderSecs = time - (mins * 60);
		if (mins < 60)
			return (mins < 10 ? "0" : "") + mins + mMark + " " + (remainderSecs < 10 ? "0" : "") + remainderSecs
					+ sMark;

		long hours = mins / 60;
		long remainderMins = mins - (hours * 60);
		return (hours < 10 ? "0" : "") + hours + RageMode.getLang().get("time-formats.hour") + " "
				+ (remainderMins < 10 ? "0" : "") + remainderMins + mMark + " " + (remainderSecs < 10 ? "0" : "")
				+ remainderSecs + sMark;
	}

	/**
	 * Calls an event.
	 * <p>
	 * If the event is not Asynchronous, it performs on the main thread, preventing
	 * async catch.
	 * 
	 * @param event {@link org.bukkit.event.Event}
	 */
	public static void callEvent(org.bukkit.event.Event event) {
		if (!hu.montlikadani.ragemode.API.RageModeAPI.getPlugin().isEnabled()) {
			return;
		}

		if (!event.isAsynchronous() && !Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().callSyncMethod(RageMode.getInstance(), () -> {
				Bukkit.getPluginManager().callEvent(event);
				return true;
			});
		} else {
			Bukkit.getPluginManager().callEvent(event);
		}
	}

	public static CompletableFuture<Boolean> teleport(Entity entity, Location loc) {
		final CompletableFuture<Boolean> comp = new CompletableFuture<>();
		final ServerSoftwareType softwareType = RageMode.getSoftwareType();

		if (softwareType != ServerSoftwareType.UNKNOWN && softwareType != ServerSoftwareType.SPIGOT) {
			if (!Bukkit.isPrimaryThread()) { // Perform on main thread to prevent async catchop
				Bukkit.getScheduler().runTaskLater(RageMode.getInstance(),
						() -> entity.teleportAsync(loc).thenAccept(done -> {
							if (!done) {
								entity.teleport(loc);
							}
						}), 1L);
			} else {
				entity.teleportAsync(loc).thenAccept(done -> {
					if (!done) {
						entity.teleport(loc);
					}
				});
			}
		} else if (!Bukkit.isPrimaryThread()) { // Check if current thread is async
			Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () -> entity.teleport(loc));
		} else {
			entity.teleport(loc);
		}

		comp.complete(true);
		return comp;
	}

	public static Collection<Entity> getNearbyEntities(org.bukkit.World w, Location loc, int radius) {
		return Version.isCurrentHigher(Version.v1_8_R1) ? w.getNearbyEntities(loc, radius, radius, radius)
				: getNearbyList(loc, radius);
	}

	public static List<Entity> getNearbyEntities(Entity e, int radius) {
		return Version.isCurrentHigher(Version.v1_8_R1) ? e.getNearbyEntities(radius, radius, radius)
				: getNearbyList(e.getLocation(), radius);
	}

	private static List<Entity> getNearbyList(Location loc, int radius) {
		List<Entity> radiusEntities = new java.util.ArrayList<>();
		if (loc.getWorld() == null) {
			return radiusEntities;
		}

		int chunkRadius = radius < 16 ? 1 : radius / 16;
		for (int chunkX = 0 - chunkRadius; chunkX <= chunkRadius; chunkX++) {
			for (int chunkZ = 0 - chunkRadius; chunkZ <= chunkRadius; chunkZ++) {
				int x = (int) loc.getX(), y = (int) loc.getY(), z = (int) loc.getZ();

				for (Entity e : new Location(loc.getWorld(), x + chunkX * 16, y, z + chunkZ * 16).getChunk()
						.getEntities()) {
					if (loc.getWorld().getName().equalsIgnoreCase(e.getWorld().getName())
							&& e.getLocation().distanceSquared(loc) <= radius * radius
							&& e.getLocation().getBlock() != loc.getBlock()) {
						radiusEntities.add(e);
					}
				}
			}
		}

		return radiusEntities;
	}

	public static void clearPlayerInventory(Player pl) {
		pl.getInventory().clear();
		pl.updateInventory();
	}

	public static String setPlaceholders(String s, Player player) {
		if (player == null) {
			return colors(s);
		}

		PlayerPoints pp = RuntimePPManager.getPPForPlayer(player.getUniqueId());
		if (GameUtils.isPlayerPlaying(Bukkit.getPlayer(player.getUniqueId()))) {
			pp = RageScores.getPlayerPoints(player.getUniqueId()).orElse(pp);
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
			s = s.replace("%kd%", NumberFormat.getInstance(Locale.ENGLISH).format(kd));
		}

		if (s.contains("%current-streak%"))
			s = s.replace("%current-streak%", pp == null ? "0" : Integer.toString(pp.getCurrentStreak()));

		if (s.contains("%longest-streak%"))
			s = s.replace("%longest-streak%", pp == null ? "0" : Integer.toString(pp.getLongestStreak()));

		if (s.contains("%points%"))
			s = s.replace("%points%", pp == null ? "0" : Integer.toString(pp.getPoints()));

		if (s.contains("%games%")) {
			s = s.replace("%games%", Integer.toString(pp == null ? 0 : pp.getGames()));
		}

		if (s.contains("%wins%")) {
			s = s.replace("%wins%", Integer.toString(pp == null ? 0 : pp.getWins()));
		}

		return colors(s);
	}

	public static List<String> colorList(List<String> list) {
		return list.stream().map(Utils::colors).collect(Collectors.toList());
	}

	public static String colors(String s) {
		if (s == null) {
			return "";
		}

		if (Version.isCurrentEqualOrHigher(Version.v1_16_R1) && s.contains("#")) {
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

	public static void connectToHub(Player player) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(hu.montlikadani.ragemode.config.ConfigValues.getHubName());
		player.sendPluginMessage(RageMode.getInstance(), "BungeeCord", out.toByteArray());
	}

	public static boolean isDouble(String str) {
		if (str == null) {
			return false;
		}

		try {
			Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	public static boolean isInt(String str) {
		if (str == null) {
			return false;
		}

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
				return iChatBaseComponent.cast(m.invoke(chatSerializer, "{\"text\":\"" + name + "\"}"));
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