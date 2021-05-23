package hu.montlikadani.ragemode.utils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;

public class Utils {

	private static final RageMode RM = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	public static String getFormattedTime(long time) {
		String second = RageMode.getLang().get("time-formats.second");

		if (time < 60)
			return time + second;

		String minute = RageMode.getLang().get("time-formats.minute");

		long mins = time / 60;
		long remainderSecs = time - (mins * 60);

		if (mins < 60) {
			return (mins < 10 ? "0" : "") + mins + minute + " " + (remainderSecs < 10 ? "0" : "") + remainderSecs
					+ second;
		}

		long hours = mins / 60;
		long remainderMins = mins - (hours * 60);

		return (hours < 10 ? "0" : "") + hours + RageMode.getLang().get("time-formats.hour") + " "
				+ (remainderMins < 10 ? "0" : "") + remainderMins + minute + " " + (remainderSecs < 10 ? "0" : "")
				+ remainderSecs + second;
	}

	/**
	 * Calls an event depending on the current thread.
	 * <p>
	 * If the event is not Asynchronous, it performs on the main thread, preventing
	 * async catch.
	 * 
	 * @param event {@link org.bukkit.event.Event}
	 */
	public static void callEvent(org.bukkit.event.Event event) {
		if (!RM.isEnabled()) {
			return;
		}

		if (!event.isAsynchronous() && !RM.getServer().isPrimaryThread()) {
			SchedulerUtil.submitSync(() -> RM.getServer().getPluginManager().callEvent(event), true);
		} else {
			RM.getServer().getPluginManager().callEvent(event);
		}
	}

	public static CompletableFuture<Boolean> teleport(Entity entity, Location loc) {
		final CompletableFuture<Boolean> comp = new CompletableFuture<>();

		if (RM.isPaper()) {
			if (!RM.getServer().isPrimaryThread()) { // Perform on main thread to prevent async catchop
				// Call synchronously without delay
				SchedulerUtil.submitSync(() -> entity.teleportAsync(loc).thenAccept(done -> {
					if (!done.booleanValue()) {
						entity.teleport(loc);
					}

					comp.complete(true);
				}), true);
			} else {
				entity.teleportAsync(loc).whenComplete((done, throwable) -> {
					if (!done.booleanValue()) {
						entity.teleport(loc);
					}

					comp.complete(true);
				});
			}
		} else if (!RM.getServer().isPrimaryThread()) { // Check if current thread is async
			// Call synchronously without delay
			SchedulerUtil.submitSync(() -> entity.teleport(loc), comp.complete(true));
		} else {
			entity.teleport(loc);
			comp.complete(true);
		}

		return comp;
	}

	public static java.util.Collection<Entity> getNearbyEntities(org.bukkit.World w, Location loc, int radius) {
		return ServerVersion.isCurrentHigher(ServerVersion.v1_8_R1) ? w.getNearbyEntities(loc, radius, radius, radius)
				: getNearbyList(loc, radius);
	}

	public static List<Entity> getNearbyEntities(Entity e, int radius) {
		return ServerVersion.isCurrentHigher(ServerVersion.v1_8_R1) ? e.getNearbyEntities(radius, radius, radius)
				: getNearbyList(e.getLocation(), radius);
	}

	private static List<Entity> getNearbyList(Location loc, int radius) {
		final List<Entity> nearbyEntities = new java.util.ArrayList<>();
		final org.bukkit.World world = loc.getWorld();

		if (world == null) {
			return nearbyEntities;
		}

		final org.bukkit.block.Block block = loc.getBlock();

		int chunkRadius = radius < 16 ? 1 : radius / 16;

		radius *= radius;

		for (int chunkX = -chunkRadius; chunkX <= chunkRadius; chunkX++) {
			for (int chunkZ = -chunkRadius; chunkZ <= chunkRadius; chunkZ++) {
				int x = (int) loc.getX(), y = (int) loc.getY(), z = (int) loc.getZ();

				for (Entity e : new Location(world, x + chunkX * 16, y, z + chunkZ * 16).getChunk().getEntities()) {
					if (world.getName().equalsIgnoreCase(e.getWorld().getName())
							&& e.getLocation().distanceSquared(loc) <= radius && e.getLocation().getBlock() != block) {
						nearbyEntities.add(e);
					}
				}
			}
		}

		return nearbyEntities;
	}

	public static String setPlaceholders(String s, Player player) {
		if (player == null) {
			return colors(s);
		}

		PlayerPoints pp = RuntimePPManager.getPPForPlayer(player.getUniqueId());

		if (GameUtils.getGameByPlayer(player.getUniqueId()) != null) {
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
			s = s.replace("%kd%", java.text.NumberFormat.getInstance(java.util.Locale.ENGLISH).format(kd));
		}

		if (s.contains("%current-streak%"))
			s = s.replace("%current-streak%", pp == null ? "0" : Integer.toString(pp.getCurrentStreak()));

		if (s.contains("%longest-streak%"))
			s = s.replace("%longest-streak%", pp == null ? "0" : Integer.toString(pp.getLongestStreak()));

		if (s.contains("%points%"))
			s = s.replace("%points%", pp == null ? "0" : Integer.toString(pp.getPoints()));

		if (s.contains("%games%")) {
			s = s.replace("%games%", pp == null ? "0" : Integer.toString(pp.getGames()));
		}

		if (s.contains("%wins%")) {
			s = s.replace("%wins%", pp == null ? "0" : Integer.toString(pp.getWins()));
		}

		return colors(s);
	}

	public static List<String> colorList(List<String> list) {
		int size = list.size();

		if (size > 0) {
			for (int i = 0; i < size; i++) {
				list.set(i, colors(list.get(i)));
			}
		}

		return list;
	}

	public static String colors(String s) {
		if (s == null) {
			return "";
		}

		if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_16_R1) && s.contains("#")) {
			s = matchColorRegex(s);
		}

		return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
	}

	private static final Pattern HEX_PATTERN = Pattern.compile("&?#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})");

	private static String matchColorRegex(String s) {
		java.util.regex.Matcher matcher = HEX_PATTERN.matcher(s);

		while (matcher.find()) {
			try {
				s = s.replace(matcher.group(0), net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)) + "");
			} catch (Exception e) {
			}
		}

		return s;
	}

	public static void connectToHub(Player player) {
		if (!RM.isEnabled()) {
			return;
		}

		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(hu.montlikadani.ragemode.config.configconstants.ConfigValues.getHubName());
		player.sendPluginMessage(RM, "BungeeCord", out.toByteArray());
	}

	public static Optional<Double> tryParse(String str) {
		if (str == null || str.isEmpty()) {
			return Optional.empty();
		}

		try {
			return Optional.of(Double.parseDouble(str));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	public static Optional<Integer> tryParseInt(String str) {
		if (str == null || str.isEmpty()) {
			return Optional.empty();
		}

		try {
			return Optional.of(Integer.parseInt(str));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	public static Optional<Float> tryParseFloat(String str) {
		if (str == null || str.isEmpty()) {
			return Optional.empty();
		}

		try {
			return Optional.of(Float.parseFloat(str));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}
}